package org.fabric3.introspection.xml.binding;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.BindingHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 *
 */
public class BindingHandlerLoaderTestCase extends TestCase {
    private static final String XML_VALID = "<handler xmlns='" + org.fabric3.api.Namespaces.F3 + "' target='TestComponent'/>";
    private static final String XML_NO_TARGET = "<handler xmlns='" + org.fabric3.api.Namespaces.F3 + "'/>";
    private static final String XML_INVALID_ATTRIBUTE = "<handler xmlns='" + org.fabric3.api.Namespaces.F3 + "' target='TestComponent' invalid='TestComponent'/>";

    private DefaultIntrospectionContext context;
    private BindingHandlerLoader loader;

    public void testLoad() throws Exception {
        XMLStreamReader reader = createReader(XML_VALID);

        BindingHandler definition = loader.load(reader, context);
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
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(stream);
        reader.nextTag();
        return reader;
    }


}
