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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public class Main {
    Logger log = LoggerFactory.getLogger(com.cobbinterwebs.fsm.codegenerator.Main.class);
    private static final int CL_INPUT_FILE_IDX = 0;
    private static final int CL_OUTPUT_FOLDER_IDX = 1;

    String path = null;
    String packageName = null;
    String implName = null;
    String customHandlerClass = null;
    String stateName = null;

    /**
     * Key := the Event Handler Name
     * Value := list of the supported states for the Event Handler
     */
    Map<String, ArrayList<String>> eventStatesMap = new HashMap<>();
    static Document doc = null;

    Map<String, String> handleFunctionNames = new HashMap<String,String>();

    String inputFileName = null;

    String outputFolderName = null;
    String outputFileName = null;
    static PrintWriter printWriter = null;

    public Main(String inputFileName, String outputFolderName) {
        this.inputFileName = inputFileName;
        this.outputFolderName = outputFolderName;
    }

    public static void main(String[] fileNames) {
        // check arguements. Make sure there are two.
        if(2 != fileNames.length) {
            showUsage();
            System.exit(-1);
        }

        Main generator = new Main(fileNames[CL_INPUT_FILE_IDX],fileNames[CL_OUTPUT_FOLDER_IDX]);
        generator.runGenerate();
    }


    /**
     * Abstracted main code to facilitate junit testing.
     **/
    void runGenerate() {
        try {
            openInputFile();
            initPackageClassVars();
            initFolderStructure();
            initEventHandlerStatesMap();
            dumpEventStateMap();
            openOutputFile();
            generateCode();
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

    private void initEventHandlerStatesMap() {
        NodeList eventNodeList = doc.getElementsByTagName("event");
        int eventNodeListSize = eventNodeList.getLength();
        log.info("Found {} events to process", eventNodeListSize);
        for(int eventIndex = 0; eventIndex < eventNodeListSize; eventIndex++) {
            Node eventNode = eventNodeList.item(eventIndex);
            String eventKey = eventNode.getAttributes().getNamedItem("handlerFunction").getNodeValue();
            log.info("Processing event state map for event: {}", eventKey);
            ArrayList<String> stateList = eventStatesMap.get(eventKey);
            if(null == stateList) {
                stateList = new ArrayList<String>();
                eventStatesMap.put(eventKey, stateList);
            }
            String eventStateName = eventNode.getParentNode().getAttributes().getNamedItem("name").getNodeValue();
            log.info("Processing event state map for event: {}. Adding state {}", eventKey, eventStateName);
            if(!stateList.contains(eventStateName)) {
                stateList.add(eventStateName);
            }
        }
    }

    private void initFolderStructure() throws IOException {
		path = outputFolderName + File.separatorChar;
		path = path + packageName.replace('.', '/');
		path = path + File.separatorChar;
		File filePath = new File(path);
		
		if(filePath.exists()) {
			log.info("Directory already exists: " + filePath.getCanonicalPath());
			return;
		}
		
		boolean status = filePath.mkdirs();
		
		if(status) {
            log.info("Directory created successfully");
		} else {
			log.error("could not create specified directory: {}", filePath.getCanonicalPath());
			throw new IOException();
		}
	}

	private void initPackageClassVars() {
        implName =    doc.getElementsByTagName("fsm").item(0).getAttributes().getNamedItem("implClass").getNodeValue();
        packageName = doc.getElementsByTagName("fsm").item(0).getAttributes().getNamedItem("package").getNodeValue();
        customHandlerClass = doc.getElementsByTagName("fsm").item(0).getAttributes().getNamedItem("customHandlerClass").getNodeValue();
        log.info("initPackageClassVars: implName[{}], packageName[{}]", implName, packageName);
    }
    
    private void openInputFile() throws ParserConfigurationException, IOException, SAXException {
        log.info("open input file: {}", inputFileName);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = builder.parse(new File(inputFileName));
        doc.getDocumentElement().normalize();
    }

    private void openOutputFile() throws FileNotFoundException {
        outputFileName = path + "/" + implName + ".java";
        log.info("open output file: {}", outputFileName);
        printWriter = new PrintWriter(outputFileName);
    }

    private static void showUsage() {
        System.out.println("Incorrect number of arguments . . .");
        System.out.println("\tUsage: java om.cobbinterwebs.fsm.codegenerator.Main [inputXML] [outputFolder]");
        System.out.println("\t\tinputXML is the fully qualified name of the XML file describing the finite state machine.");
        System.out.println("\t\toutputFolder is the fully qualified name of source folder/package for writing the class file.");
    }

    void generateCode() throws ParserConfigurationException, IOException, SAXException {
        log.info("start generating code");
        Node FSM = doc.getElementsByTagName("fsm").item(0);
        Node implClassNode = FSM.getAttributes().getNamedItem("implClass");
        implName = implClassNode.getNodeValue();
        
        printWriter.println("package " + packageName + ";\n\n");

        printWriter.println("/**");
        printWriter.println(" *" );
        printWriter.println(" * GENERATED: do not edit." );
        printWriter.println(" *" );
        printWriter.print(" * INPUT: ");
        printWriter.println(this.inputFileName);
        printWriter.print(" * OUTPUT: ");
        printWriter.println(this.outputFolderName + "/");
        printWriter.print(" * CLASS: ");
        printWriter.println(packageName + "." + implName + ".java");
        printWriter.println(" *" );
        printWriter.println(" */");
        printWriter.println("public class " + implName + " {");
        printWriter.println("   private STATE _state;");
        printWriter.println("   private " + customHandlerClass + " customHandlerInst = new "+ customHandlerClass + "();\n\n");
        printWriter.println("   public " + implName + "() {\n      _state = STATE.closed;\n   }\n");
        printWriter.println("   public STATE getState() {\n      return _state;\n   }");
        processStateNames();
        printWriter.println("}");
    }

    private void processStateNames() {
        log.info("collect all the state names.");
        NodeList states = doc.getElementsByTagName("state");
        printWriter.print("   public enum STATE{");
        int stateLen = states.getLength();
        for (int i = 0; i < stateLen; i++) {
            Node aStateNode = states.item(i);
            NamedNodeMap attributeMap = aStateNode.getAttributes();
            stateName = attributeMap.getNamedItem("name").getNodeValue();
            log.info("\tstate[{}]", stateName);
            printWriter.print(stateName);
            if(i == stateLen - 1) {
                continue;
            }
            printWriter.print(",");
        }
        printWriter.println("};\n");

        for (int i = 0; i < stateLen; i++) {
           Node aStateNode = states.item(i);
            NamedNodeMap attributeMap = aStateNode.getAttributes();
            stateName = attributeMap.getNamedItem("name").getNodeValue();
            processEventsForAState(aStateNode);
        }
    }

    /**
     * Populates a list of states that can be processed by a specific handler.
     */
    private void collectStatesForEvents() {

    }

    private void processEventsForAState(Node stateNode) {
        String eventName = "FEED FACE";
        log.info("process events for a given state[{}]",stateName);
        NodeList eventNodes = stateNode.getChildNodes();
        int eventLen = eventNodes.getLength();
        for (int i = 0; i < eventLen; i++) {
            Node eventNode = eventNodes.item(i);
            if (eventNode.hasAttributes()) {
                NamedNodeMap attrs = eventNode.getAttributes();
                String handlerNameFunction = attrs.getNamedItem("handlerFunction").getNodeValue();
                eventName = attrs.getNamedItem("name").getNodeValue();
                log.debug("\t\teventName[{}], handlerFunction[{}]", eventName,handlerNameFunction);
                if(null != handleFunctionNames.get(handlerNameFunction)) {
                    log.info("SKIPPED: process events for a given state[{}], handlerNameFunction[{}]",stateName, handlerNameFunction);
                    continue;
                }
                handleFunctionNames.put(handlerNameFunction, handlerNameFunction);
                //<event name="timeout" resultState="closed" handlerFunction="Timeout"/>
                log.info("\tgenerating code for eventName[{}]. handledFunction[{}]", eventName, handlerNameFunction);
                printWriter.println("   public void " + attrs.getNamedItem("handlerFunction").getNodeValue() + "() throws IllegalStateException{");
                // TODO 10NOV2022 DLM Process the list of acceptable states from the eventStateMap instead of the current state we are processing.
                printWriter.println("      if( this._state != STATE." + this.stateName + "){");
                printWriter.println("         throw new IllegalStateException();");
                printWriter.println("      }\n");
                printWriter.println("      try {");
                printWriter.println("         customHandlerInst." + attrs.getNamedItem("handlerFunction").getNodeValue() + "();" );
                printWriter.println("      } catch(Throwable t) {");
                printWriter.println("         throw new IllegalStateException(t);");
                printWriter.println("      }\n");
                printWriter.println("      _state = STATE." + attrs.getNamedItem("resultState").getNodeValue() + ";");
                printWriter.println("   }\n");
            }
        }
    }

    public String toString() {
        StringBuilder buf = new StringBuilder(this.getClass().getName());
        buf.append("[").append(this.inputFileName).append("]");
        return buf.toString();
    }

    private void dumpEventStateMap() {
        this.eventStatesMap.keySet().stream().iterator().forEachRemaining(new Consumer<String>() {
            @Override
            public void accept(String s) {
                logAcceptableStatesForEvent(s);
            }
        });
    }
    private void logAcceptableStatesForEvent(String key) {
        log.info("Acceptable States For Event: {}", key);
        eventStatesMap.get(key).forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                log.info("\t\t{}", s);
            }
        });
    }
}
