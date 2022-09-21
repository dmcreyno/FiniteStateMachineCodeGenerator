package com.cobbinterwebs.fsm.codegenerator;

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

public class Main {

    private static final int CL_INPUT_FILE_IDX = 0;
    private static final int CL_OUTPUT_FOLDER_IDX = 1;

    String implName = null;
    String stateName = null;
    Document doc = null;

    Map handleFunctionNames = new HashMap<String,String>();

    String inputFileName = null;

    String outputFolderName = null;
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
        try {
            generator.openInputFile();
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

    private void openInputFile() throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        doc = builder.parse(new File(inputFileName));
        doc.getDocumentElement().normalize();
    }

    private void openOutputFile() throws FileNotFoundException {
        Node FSM = doc.getElementsByTagName("fsm").item(0);
        Node implClassNode = FSM.getAttributes().getNamedItem("implClass");
        implName = implClassNode.getNodeValue();
        this.printWriter = new PrintWriter(outputFolderName + "/" + implName + ".java");
    }

    private static void showUsage() {
        System.out.println("Incorrect number of arguments . . .");
        System.out.println("\tUsage: java om.cobbinterwebs.fsm.codegenerator.Main [inputXML] [outputFolder]");
        System.out.println("\t\tinputXML is the fully qualified name of the XML file describing the finite state machine.");
        System.out.println("\t\toutputFolder is the fully qualified name of source folder/package for writing the class file.");
    }

    void generateCode() throws ParserConfigurationException, IOException, SAXException {
        Node FSM = doc.getElementsByTagName("fsm").item(0);
        Node implClassNode = FSM.getAttributes().getNamedItem("implClass");
        implName = implClassNode.getNodeValue();
        printWriter.println("public class " + implName + " {");
        printWriter.println("   private STATE _state;");
        processStateNames();
        printWriter.println("}");
    }

    private void processStateNames() {
        NodeList states = doc.getElementsByTagName("state");
        printWriter.print("   private enum STATE{");
        int stateLen = states.getLength();
        for (int i = 0; i < stateLen; i++) {
            Node aStateNode = states.item(i);
            NamedNodeMap attributeMap = aStateNode.getAttributes();
            stateName = attributeMap.getNamedItem("name").getNodeValue();
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

    private void processEventsForAState(Node stateNode) {
        NodeList eventNodes = stateNode.getChildNodes();
        int eventLen = eventNodes.getLength();
        for (int i = 0; i < eventLen; i++) {
            Node eventNode = eventNodes.item(i);
            if (eventNode.hasAttributes()) {
                NamedNodeMap attrs = eventNode.getAttributes();
                String handlerNameFunction = attrs.getNamedItem("handlerFunction").getNodeValue();
                if(null != handleFunctionNames.get(handlerNameFunction)) {
                    continue;
                }
                handleFunctionNames.put(handlerNameFunction, handlerNameFunction);
                //<event name="timeout" resultState="closed" handlerFunction="Timeout"/>
                printWriter.println("   public void " + attrs.getNamedItem("handlerFunction").getNodeValue() + "() throws IllegalStateException{");
                printWriter.println("      if( this._state != STATE." + this.stateName + "){");
                printWriter.println("         throw new IllegalStateException();");
                printWriter.println("      }\n");
                printWriter.println("      _state = STATE." + attrs.getNamedItem("resultState").getNodeValue() + ";");
                printWriter.println("   }\n");
            }
        }
    }
}
