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
package org.fabric3.implementation.java.introspection;

import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.oasisopen.sca.annotation.Reference;

/**
 * Adds metadata for Java component implementations.
 */
public class JavaImplementationProcessor extends AbstractPojoImplementationProcessor {
    public JavaImplementationProcessor(@Reference JavaContractProcessor processor,
                                       @Reference JavaImplementationIntrospector introspector,
                                       @Reference IntrospectionHelper helper) {
        super(processor, introspector, helper);
    }

    protected JavaImplementation createImplementation(Class<?> clazz, IntrospectionContext context) {
        JavaImplementation implementation = new JavaImplementation();
        implementation.setImplementationClass(clazz.getName());
        InjectingComponentType componentType = new InjectingComponentType(clazz.getName());
        implementation.setComponentType(componentType);
        return implementation;
    }

}
