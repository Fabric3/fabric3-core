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

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.implementation.junit.model.JUnitBinding;
import org.fabric3.implementation.junit.model.JUnitImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
@Key("junit")
public class JUnitImplementationProcessor implements ImplementationProcessor<JUnitImplementation> {
    private JUnitImplementationIntrospector introspector;

    public JUnitImplementationProcessor(@Reference JUnitImplementationIntrospector introspector) {
        this.introspector = introspector;
    }

    public void process(Component<JUnitImplementation> component, IntrospectionContext context) {
        JUnitImplementation implementation = component.getImplementation();
        InjectingComponentType componentType = implementation.getComponentType();
        introspector.introspect(componentType, context);

        // Add a binding only on the JUnit service (which is the impl class) so wires are generated to the test operations.
        // These wires will be used by the testing runtime to dispatch to the JUnit components.
        for (Service<ComponentType> service : componentType.getServices().values()) {
            if (service.getServiceContract().getQualifiedInterfaceName().equals(implementation.getImplementationClass().getName())) {
                JUnitBinding bindingDefinition = new JUnitBinding(null);
                service.addBinding(bindingDefinition);
                break;
            }
        }
    }

    public void process(Component<JUnitImplementation> component, Class<?> clazz, IntrospectionContext context) {
        JUnitImplementation implementation = new JUnitImplementation(clazz);
        InjectingComponentType componentType = new InjectingComponentType(clazz.getName());
        implementation.setComponentType(componentType);
        component.setImplementation(implementation);
        process(component, context);

    }

}
