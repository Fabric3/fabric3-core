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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.introspection.java.annotation;

import junit.framework.TestCase;

import org.fabric3.api.annotation.scope.Composite;
import org.fabric3.api.annotation.scope.Scopes;
import org.fabric3.api.model.type.java.InjectingComponentType;

@SuppressWarnings("unchecked")
public class CompositeProcessorTestCase extends TestCase {

    public void testScopeIdentification() throws Exception {

        CompositeAnnotated componentToProcess = new CompositeAnnotated();
        Composite annotation = componentToProcess.getClass().getAnnotation(Composite.class);
        CompositeProcessor processor = new CompositeProcessor();
        InjectingComponentType type = new InjectingComponentType();
        processor.visitType(annotation, componentToProcess.getClass(), type, null);

        assertEquals(Scopes.COMPOSITE, type.getScope());
    }

    @Composite
    public static class CompositeAnnotated {

    }

}
