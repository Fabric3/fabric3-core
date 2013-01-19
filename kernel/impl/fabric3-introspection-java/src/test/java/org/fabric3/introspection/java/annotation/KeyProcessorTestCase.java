package org.fabric3.introspection.java.annotation;

import junit.framework.TestCase;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 *
 */
public class KeyProcessorTestCase extends TestCase {
    private KeyProcessor processor;
    private InjectingComponentType type;
    private DefaultIntrospectionContext context;

    public void testParseKey() throws Exception {
        KeyAnnotated annotated = new KeyAnnotated();
        Key annotation = annotated.getClass().getAnnotation(Key.class);


        processor.visitType(annotation, annotated.getClass(), type, context);

        assertEquals("test", type.getKey());
    }

    public void testInvalidKey() throws Exception {
        InvalidAnnotated annotated = new InvalidAnnotated();
        Key annotation = annotated.getClass().getAnnotation(Key.class);

        processor.visitType(annotation, annotated.getClass(), type, context);

        assertTrue(context.hasErrors());
        assertTrue(context.getErrors().get(0) instanceof InvalidAnnotation);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        processor = new KeyProcessor();
        type = new InjectingComponentType();
        context = new DefaultIntrospectionContext();
    }

    @Key("test")
    private static class KeyAnnotated {

    }

    @Key()
    private static class InvalidAnnotated {

    }

}
