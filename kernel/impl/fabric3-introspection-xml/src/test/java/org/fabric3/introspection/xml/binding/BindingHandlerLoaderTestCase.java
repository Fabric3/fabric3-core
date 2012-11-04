package org.fabric3.introspection.xml.binding;

import java.io.ByteArrayInputStream;
import java.net.URI;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.fabric3.host.Namespaces;
import org.fabric3.introspection.xml.MockXMLFactory;
import org.fabric3.spi.model.type.binding.BindingHandlerDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 *
 */
public class BindingHandlerLoaderTestCase extends TestCase {
    private static final String XML_VALID = "<handler xmlns='" + Namespaces.F3 + "' target='TestComponent'/>";
    private static final String XML_NO_TARGET = "<handler xmlns='" + Namespaces.F3 + "'/>";
    private static final String XML_INVALID_ATTRIBUTE = "<handler xmlns='" + Namespaces.F3 + "' target='TestComponent' invalid='TestComponent'/>";

    private DefaultIntrospectionContext context;
    private BindingHandlerLoader loader;
    private MockXMLFactory factory;


    public void testLoad() throws Exception {
        XMLStreamReader reader = createReader(XML_VALID);

        BindingHandlerDefinition definition = loader.load(reader, context);
        assertEquals(URI.create("TestComponent"), definition.getTarget());
    }

    public void testNoTarget() throws Exception {
        XMLStreamReader reader = createReader(XML_NO_TARGET);

        loader.load(reader, context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidValue);
    }

    public void testInvalidAttribute() throws Exception {
        XMLStreamReader reader = createReader(XML_INVALID_ATTRIBUTE);

        loader.load(reader, context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof UnrecognizedAttribute);
    }

    protected void setUp() throws Exception {
        super.setUp();
        context = new DefaultIntrospectionContext();
        loader = new BindingHandlerLoader();

        factory = new MockXMLFactory();
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = factory.newInputFactoryInstance().createXMLStreamReader(stream);
        reader.nextTag();
        return reader;
    }


}
