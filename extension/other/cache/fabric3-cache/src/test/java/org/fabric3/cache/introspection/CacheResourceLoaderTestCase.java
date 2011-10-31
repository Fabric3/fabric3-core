package org.fabric3.cache.introspection;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.cache.model.CacheSetResourceDefinition;
import org.fabric3.cache.spi.CacheResourceDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 * @version $Rev$ $Date$
 */
public class CacheResourceLoaderTestCase extends TestCase {
    private static final String XML = "<caches><cache name='cache'><provider/></cache></caches>";
    private static final String XML_NO_NAME = "<caches><cache><provider/></cache></caches>";

    private CacheResourceLoader loader;
    private LoaderRegistry registry;
    private IntrospectionContext context;
    private XMLInputFactory factory;

    public void testLoad() throws Exception {
        CacheResourceDefinition resourceDefinition = new CacheResourceDefinition() {
        };
        EasyMock.expect(registry.load(EasyMock.isA(XMLStreamReader.class),
                                      EasyMock.eq(CacheResourceDefinition.class),
                                      EasyMock.isA(IntrospectionContext.class))).andReturn(resourceDefinition);
        EasyMock.replay(registry);

        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        reader.nextTag();

        CacheSetResourceDefinition definition = loader.load(reader, context);

        assertFalse(context.hasErrors());
        assertEquals(1, definition.getDefinitions().size());
        CacheResourceDefinition cacheDefinition = definition.getDefinitions().get(0);
        assertEquals("cache", cacheDefinition.getCacheName());
    }

    public void testLoadNoName() throws Exception {
        CacheResourceDefinition resourceDefinition = new CacheResourceDefinition() {
        };
        EasyMock.expect(registry.load(EasyMock.isA(XMLStreamReader.class),
                                      EasyMock.eq(CacheResourceDefinition.class),
                                      EasyMock.isA(IntrospectionContext.class))).andReturn(resourceDefinition);
        EasyMock.replay(registry);

        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML_NO_NAME.getBytes()));
        reader.nextTag();

        loader.load(reader, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof MissingAttribute);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        registry = EasyMock.createMock(LoaderRegistry.class);
        loader = new CacheResourceLoader(registry);

        factory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext();

    }
}
