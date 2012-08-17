package org.fabric3.cache.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.annotation.Cache;
import org.fabric3.cache.model.CacheReferenceDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class CacheProcessorTestCase extends TestCase {
    private CacheProcessor processor;
    private IntrospectionContext context;
    private InjectingComponentType componentType;
    private JavaContractProcessor contractProcessor;
    private IntrospectionHelper helper;

    @SuppressWarnings({"unchecked"})
    public void testField() throws Exception {

        JavaServiceContract contract = new JavaServiceContract();
        EasyMock.expect(contractProcessor.introspect(EasyMock.isA(Class.class),
                                                     EasyMock.isA(IntrospectionContext.class))).andReturn(contract);

        EasyMock.expect(helper.getSiteName(EasyMock.isA(Field.class), (String) EasyMock.isNull())).andReturn("cache");
        EasyMock.replay(contractProcessor, helper);

        Field field = Foo.class.getDeclaredField("cache");
        Cache annotation = field.getAnnotation(Cache.class);
        processor.visitField(annotation, field, Foo.class, componentType, context);

        assertFalse(context.hasErrors());
        CacheReferenceDefinition definition = (CacheReferenceDefinition) componentType.getResourceReferences().get("cache");
        assertEquals("cache", definition.getCacheName());

        EasyMock.verify(contractProcessor, helper);
    }

    @SuppressWarnings({"unchecked"})
    public void testMethod() throws Exception {

        JavaServiceContract contract = new JavaServiceContract();
        EasyMock.expect(contractProcessor.introspect(EasyMock.isA(Class.class),
                                                     EasyMock.isA(IntrospectionContext.class))).andReturn(contract);

        EasyMock.expect(helper.getSiteName(EasyMock.isA(Method.class), (String) EasyMock.isNull())).andReturn("cache");
        EasyMock.replay(contractProcessor, helper);

        Method method = Foo.class.getDeclaredMethod("setCache", Map.class);
        Cache annotation = method.getAnnotation(Cache.class);
        processor.visitMethod(annotation, method, Foo.class, componentType, context);

        assertFalse(context.hasErrors());
        CacheReferenceDefinition definition = (CacheReferenceDefinition) componentType.getResourceReferences().get("cache");
        assertEquals("cache", definition.getCacheName());

        EasyMock.verify(contractProcessor, helper);
    }

    @SuppressWarnings({"unchecked"})
    public void testConstructor() throws Exception {

        JavaServiceContract contract = new JavaServiceContract();
        EasyMock.expect(contractProcessor.introspect(EasyMock.isA(Class.class),
                                                     EasyMock.isA(IntrospectionContext.class))).andReturn(contract);

        EasyMock.replay(contractProcessor, helper);

        Constructor<Foo> constructor = Foo.class.getConstructor(Map.class);
        Cache annotation = (Cache) constructor.getParameterAnnotations()[0][0];
        processor.visitConstructorParameter(annotation, constructor, 0, Foo.class, componentType, context);

        assertFalse(context.hasErrors());
        CacheReferenceDefinition definition = (CacheReferenceDefinition) componentType.getResourceReferences().get("cache");
        assertEquals("cache", definition.getCacheName());

        EasyMock.verify(contractProcessor, helper);
    }


    @SuppressWarnings({"unchecked"})
    @Override
    public void setUp() throws Exception {
        super.setUp();
        helper = EasyMock.createMock(IntrospectionHelper.class);
        contractProcessor = EasyMock.createMock(JavaContractProcessor.class);
        processor = new CacheProcessor(contractProcessor, helper);
        componentType = new InjectingComponentType();
        context = new DefaultIntrospectionContext();
    }


    private static class Foo {

        @Cache(name = "cache")
        protected Map cache;

        @Cache(name = "cache")
        public void setCache(Map cache) {

        }

        public Foo(@Cache(name = "cache") Map cache) {
            this.cache = cache;
        }
    }


}
