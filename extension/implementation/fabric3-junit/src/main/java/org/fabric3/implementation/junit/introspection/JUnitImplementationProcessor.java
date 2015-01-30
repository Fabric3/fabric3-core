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
package org.fabric3.implementation.junit.introspection;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.implementation.junit.model.JUnitBindingDefinition;
import org.fabric3.implementation.junit.model.JUnitImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class JUnitImplementationProcessor implements ImplementationProcessor<JUnitImplementation> {
    private JUnitImplementationIntrospector introspector;

    public JUnitImplementationProcessor(@Reference JUnitImplementationIntrospector introspector) {
        this.introspector = introspector;
    }

    public void process(ComponentDefinition<JUnitImplementation> definition, IntrospectionContext context) {
        JUnitImplementation implementation = definition.getImplementation();
        InjectingComponentType componentType = implementation.getComponentType();
        introspector.introspect(componentType, context);

        // Add a binding only on the JUnit service (which is the impl class) so wires are generated to the test operations.
        // These wires will be used by the testing runtime to dispatch to the JUnit components.
        for (ServiceDefinition<ComponentType> serviceDefinition : componentType.getServices().values()) {
            if (serviceDefinition.getServiceContract().getQualifiedInterfaceName().equals(implementation.getImplementationClass())) {
                JUnitBindingDefinition bindingDefinition = new JUnitBindingDefinition(null);
                serviceDefinition.addBinding(bindingDefinition);
                break;
            }
        }
    }

    public void process(ComponentDefinition<JUnitImplementation> definition, Class<?> clazz, IntrospectionContext context) {
        String name = clazz.getName();
        JUnitImplementation implementation = new JUnitImplementation(name);
        InjectingComponentType componentType = new InjectingComponentType(name);
        implementation.setComponentType(componentType);
        definition.setImplementation(implementation);
        process(definition, context);

    }

}
