package com.cobbinterwebs.fsm.codegenerator;

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

import static org.junit.jupiter.api.Assertions.fail;

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
    public void testGenerator() {
        com.cobbinterwebs.fsm.codegenerator.Main app = new com.cobbinterwebs.fsm.codegenerator.Main(
                "src/test/resources/fsm-tcpip.xml",
                "src/generated");
        log.info("created instance of generator: {}", app.toString());
        app.runGenerate();
    }
}
