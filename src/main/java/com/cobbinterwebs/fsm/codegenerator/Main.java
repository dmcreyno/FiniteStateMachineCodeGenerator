package com.cobbinterwebs.fsm.codegenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Reads a definition of a Finite State Machine from an XML file. Generates code
 * for the events to move state.
 * 
 * The README file has details and examples.
 * 
 * @author Cobb Interwebs, LLC
 *
 */
public class Main {
	
	private final Logger log = LoggerFactory.getLogger("");

    private static final int CL_INPUT_FILE_IDX = 0;
    private static final int CL_OUTPUT_FOLDER_IDX = 1;

    String path = null;
    String packageName = null;
    String implName = null;
    String stateName = null;
    static Document doc = null;

    Map<String, String> handleFunctionNames = new HashMap<String,String>();

    String inputFileName = null;

    String outputFolderName = null;
    static PrintWriter printWriter = null;

    public Main(String inputFileName, String outputFolderName) {
    	log.info("Creating application with arguments: \"{}\" and \"{}\".", inputFileName, outputFolderName);
        this.inputFileName = inputFileName;
        this.outputFolderName = outputFolderName;
    }

    public static void main(String[] fileNames) {
        // check arguments. Make sure there are two.
        if(2 != fileNames.length) {
            showUsage();
            System.exit(-1);
        }

       Main generator = new Main(fileNames[CL_INPUT_FILE_IDX],fileNames[CL_OUTPUT_FOLDER_IDX]);
        try {
            generator.openInputFile();
            generator.initPackageClassVars();
            generator.initFolderStructure();
            generator.openOutputFile();
            generator.generateCode();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } finally {
            if(printWriter != null) {
                printWriter.flush();
                printWriter.close();
            }
        }
    }

    private void initFolderStructure() throws IOException {
		path = outputFolderName + File.separatorChar;
		path = path + packageName.replace('.', '/');
		path = path + File.separatorChar;
		File filePath = new File(path);
		
		if(filePath.exists()) {
			log.info("Directory already exists: {}", filePath.getCanonicalPath());
			return;
		}
		
		boolean status = filePath.mkdirs();
		
		if(status) {
			log.info("Directory created successfully: {}", filePath.getCanonicalPath());
		} else {
			log.info("Sorry couldnt create specified directory: {}", filePath.getCanonicalPath());
			throw new IOException();
		}
	}

	private void initPackageClassVars() {
        implName =    doc.getElementsByTagName("fsm").item(0).getAttributes().getNamedItem("implClass").getNodeValue();
        packageName = doc.getElementsByTagName("fsm").item(0).getAttributes().getNamedItem("package").getNodeValue();
        log.info("implName: {}", implName);
        log.info("packageName: {}", packageName);
    }
    
    private void openInputFile() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = builder.parse(new File(inputFileName));
        doc.getDocumentElement().normalize();
        log.info("opened xml document: {}", inputFileName);
    }

    private void openOutputFile() throws FileNotFoundException {
        printWriter = new PrintWriter(path + "/" + implName + ".java");
        log.info("opened java class file: {}", path + "/" + implName + ".java");
    }

    private static void showUsage() {
        System.out.println("Incorrect number of arguments . . .");
        System.out.println("\tUsage: java om.cobbinterwebs.fsm.codegenerator.Main [inputXML] [outputFolder]");
        System.out.println("\t\tinputXML is the fully qualified name of the XML file describing the finite state machine.");
        System.out.println("\t\toutputFolder is the fully qualified name of source folder/package for writing the class file.");
    }

    void generateCode() throws ParserConfigurationException, IOException, SAXException {
    	log.info("generateCode");
        Node FSM = doc.getElementsByTagName("fsm").item(0);
        Node implClassNode = FSM.getAttributes().getNamedItem("implClass");
        implName = implClassNode.getNodeValue();
        
        printWriter.println("package " + packageName + ";\n\n");
    	printWriter.println("/**");
    	printWriter.println(" * If generator is executed this file will be overwritten.");
    	printWriter.println(" *");
    	printWriter.println(" * @author Cobb Interwebs, LLC.");
    	printWriter.println(" */");
        printWriter.println("public class " + implName + " {");
        printWriter.println("   private STATE _state;");
        processStateNames();
        printWriter.println("}");
    }

    private void processStateNames() {
    	log.info("processStateNames");
        NodeList states = doc.getElementsByTagName("state");
        printWriter.print("   private enum STATE{");
        int stateLen = states.getLength();
        for (int i = 0; i < stateLen; i++) {
            Node aStateNode = states.item(i);
            NamedNodeMap attributeMap = aStateNode.getAttributes();
            stateName = attributeMap.getNamedItem("name").getNodeValue();
        	log.info("generating enum for {}", stateName );
            printWriter.print(stateName);
            if(i == stateLen - 1) {
                continue;
            }
            printWriter.print(",");
        }
        printWriter.println("};\n");

        for (int i = 0; i < stateLen; i++) {
           Node aStateNode = states.item(i);
            processEventsForAState(aStateNode);
        }
    }

    private void processEventsForAState(Node stateNode) {
        NamedNodeMap attributeMap = stateNode.getAttributes();
        stateName = attributeMap.getNamedItem("name").getNodeValue();
    	log.info("processEventsForAState: {}", stateName);
        NodeList eventNodes = stateNode.getChildNodes();
        int eventLen = eventNodes.getLength();
        for (int i = 0; i < eventLen; i++) {
            Node eventNode = eventNodes.item(i);
            if (eventNode.hasAttributes()) {
                NamedNodeMap attrs = eventNode.getAttributes();
                String handlerNameFunction = attrs.getNamedItem("handlerFunction").getNodeValue();
                if(null != handleFunctionNames.get(handlerNameFunction)) {
                    continue; // we've already processed this event once before
                }

                handleFunctionNames.put(handlerNameFunction, handlerNameFunction);
            	log.info("state, event, resultState, handlerFunction: {}, {}, {}, {}", stateName, attrs.getNamedItem("name").getNodeValue(), attrs.getNamedItem("resultState").getNodeValue(), attrs.getNamedItem("handlerFunction").getNodeValue());

            	printWriter.println("   /**");
            	printWriter.println("    *");
            	printWriter.println("    *");
            	printWriter.println("    *");
            	printWriter.println("    */");
            	printWriter.println("   public void " + attrs.getNamedItem("handlerFunction").getNodeValue() + "() throws IllegalStateException{");
                printWriter.println("      if( this._state != STATE." + this.stateName + "){");
                printWriter.println("         throw new IllegalStateException();");
                printWriter.println("      }\n");

                printWriter.println("      // TODO insert business/system logic to transition state\n");

                printWriter.println("      _state = STATE." + attrs.getNamedItem("resultState").getNodeValue() + ";");
                printWriter.println("   }\n");
            }
        }
    }
}
