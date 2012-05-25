package org.fabric3.binding.ws.loader;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Collections;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.spi.model.type.binding.BindingHandlerDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;

/**
 * @version $Rev$ $Date$
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
    private LoaderHelper helper;
    private LoaderRegistry loaderRegistry;


    public void testLoad() throws Exception {
        XMLStreamReader reader = createReader(XML_VALID);
        EasyMock.replay(helper, loaderRegistry);
        WsBindingDefinition definition = loader.load(reader, context);
        assertEquals("http://fabric3.org/TestService", definition.getTargetUri().toString());
        EasyMock.verify(helper, loaderRegistry);
    }

    public void testLoadConfigurationAndHandlers() throws Exception {
        XMLStreamReader reader = createReader(XML_CONFIG_HANDLER);
        EasyMock.expect(loaderRegistry.load(reader, Object.class, context)).andReturn(Collections.singletonMap("key1", "value1"));
        EasyMock.expect(loaderRegistry.load(reader, Object.class, context)).andReturn(new BindingHandlerDefinition(URI.create("test"))).times(2);

        EasyMock.replay(helper, loaderRegistry);

        WsBindingDefinition definition = loader.load(reader, context);
        assertEquals("http://fabric3.org/TestService", definition.getTargetUri().toString());
        assertTrue(definition.getConfiguration().containsKey("key1"));
        assertEquals(2, definition.getHandlers().size());
        EasyMock.verify(helper, loaderRegistry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        context = new DefaultIntrospectionContext();

        helper = EasyMock.createNiceMock(LoaderHelper.class);
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);

        loader = new WsBindingLoader(helper, loaderRegistry);

    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        return reader;
    }


}
