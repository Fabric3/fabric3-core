package org.fabric3.binding.ws.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.ws.model.WsBinding;
import org.fabric3.api.model.type.component.BindingHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 *
 */
public class WsBindingLoaderTestCase extends TestCase {
    private static final String XML_VALID = "<binding.ws uri='http://fabric3.org/TestService'/>";
    private static final String XML_CONFIG_HANDLER = "<binding.ws uri='http://fabric3.org/TestService'>"
            + "<configuration/>"
            + "<handler/>"
            + "<handler/>"
            + "</binding.ws>";

    private DefaultIntrospectionContext context;
    private WsBindingLoader loader;
    private LoaderRegistry loaderRegistry;


    public void testLoad() throws Exception {
        XMLStreamReader reader = createReader(XML_VALID);
        EasyMock.replay(loaderRegistry);
        WsBinding definition = loader.load(reader, context);
        assertEquals("http://fabric3.org/TestService", definition.getTargetUri().toString());
        EasyMock.verify(loaderRegistry);
    }

    public void testLoadConfigurationAndHandlers() throws Exception {
        XMLStreamReader reader = createReader(XML_CONFIG_HANDLER);
        EasyMock.expect(loaderRegistry.load(reader, Object.class, context)).andReturn(Collections.singletonMap("key1", "value1"));
        EasyMock.expect(loaderRegistry.load(reader, Object.class, context)).andReturn(new BindingHandler(URI.create("test"))).times(2);

        EasyMock.replay(loaderRegistry);

        WsBinding definition = loader.load(reader, context);
        assertEquals("http://fabric3.org/TestService", definition.getTargetUri().toString());
        assertTrue(definition.getConfiguration().containsKey("key1"));
        assertEquals(2, definition.getHandlers().size());
        EasyMock.verify(loaderRegistry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        context = new DefaultIntrospectionContext();

        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);

        loader = new WsBindingLoader(loaderRegistry);

    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        return reader;
    }


}
