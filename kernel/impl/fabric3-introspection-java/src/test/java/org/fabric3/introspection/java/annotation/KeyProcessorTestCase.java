/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fabric3.introspection.java.annotation;

import junit.framework.TestCase;
import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.java.InvalidAnnotation;

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
