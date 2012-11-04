package org.fabric3.introspection.xml.common;

import java.io.ByteArrayInputStream;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.oasisopen.sca.Constants;

import org.fabric3.introspection.xml.MockXMLFactory;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;

/**
 *
 */
public class ConfigurationLoaderTestCase extends TestCase {
    private static final String XML = "<configuration xmlns='" + Constants.SCA_NS + "'><key1>value1</key1><key2>value2</key2></configuration>";


    private DefaultIntrospectionContext context;
    private ConfigurationLoader loader;
    private MockXMLFactory factory;


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

        factory = new MockXMLFactory();
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(stream);
        reader.nextTag();
        return reader;
    }


}
