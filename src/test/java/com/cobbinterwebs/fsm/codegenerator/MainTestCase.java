package com.cobbinterwebs.fsm.codegenerator;

import com.cobbinterwebs.fsm.tcpip.TCPIP_StateMachine;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MainTestCase {
    Logger log = LoggerFactory.getLogger(MainTestCase.class);

    @Test
    public void testSchema() throws IOException, SAXException {
        URL schemaFile = Paths.get("src","main", "resources", "fsm.xsd").toUri().toURL();
        log.info(schemaFile.toString());
        Source xmlFile = new StreamSource(new File("src/test/resources/fsm-tcpip.xml"));
        log.info("xmlFile stream created: {}",xmlFile.toString());
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        try {
            validator.validate(xmlFile);
        } catch (SAXException e) {
           fail("src/test/resources/fsm-tcpip.xml", e);
        }
    }

    @Test
    public void testTCPIPGenerator() {
        com.cobbinterwebs.fsm.codegenerator.Main app = new com.cobbinterwebs.fsm.codegenerator.Main(
                "src/test/resources/fsm-tcpip.xml",
                "src/test/java");
        log.info("created instance of generator: {}", app.toString());
        app.runGenerate();
    }

    @Test
    public void testTCPIPFSM() {
        TCPIP_StateMachine mach = new TCPIP_StateMachine();
        assertEquals(TCPIP_StateMachine.STATE.closed, mach.getState());

        mach.Closed_PassiveOpen();
        assertEquals(TCPIP_StateMachine.STATE.listen, mach.getState());

        mach.Listen_Close();
        assertEquals(TCPIP_StateMachine.STATE.closed, mach.getState());
    }

    @Test
    public void testTicketGenerator() {
        com.cobbinterwebs.fsm.codegenerator.Main app = new com.cobbinterwebs.fsm.codegenerator.Main(
                "src/test/resources/fsm-troubleticket.xml",
                "src/test/java");
        log.info("created instance of generator: {}", app.toString());
        app.runGenerate();
    }
}
