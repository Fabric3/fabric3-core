package org.fabric3.introspection.xml.common;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.Map;

import junit.framework.TestCase;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.oasisopen.sca.Constants;

/**
 *
 */
public class ConfigurationLoaderTestCase extends TestCase {
    private static final String XML = "<configuration xmlns='" + Constants.SCA_NS + "'><key1>value1</key1><key2>value2</key2></configuration>";


    private DefaultIntrospectionContext context;
    private ConfigurationLoader loader;

    public void testLoad() throws Exception {
        XMLStreamReader reader = createReader(XML);

        Map<String, String> configuration = loader.load(reader, context);
        assertEquals(2, configuration.size());
        assertEquals("value1", configuration.get("key1"));
        assertEquals("value2", configuration.get("key2"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        context = new DefaultIntrospectionContext();
        loader = new ConfigurationLoader();
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(stream);
        reader.nextTag();
        return reader;
    }


}
