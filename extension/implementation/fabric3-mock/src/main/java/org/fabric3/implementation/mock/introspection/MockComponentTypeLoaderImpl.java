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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.mock.introspection;

import java.util.List;

import org.easymock.IMocksControl;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.MissingResource;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 *
 */
public class MockComponentTypeLoaderImpl implements MockComponentTypeLoader {
    private final JavaContractProcessor contractProcessor;
    private final ServiceDefinition controlService;

    public MockComponentTypeLoaderImpl(@Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
        IntrospectionContext context = new DefaultIntrospectionContext();
        ServiceContract controlServiceContract = contractProcessor.introspect(IMocksControl.class, context);
        assert !context.hasErrors(); // should not happen
        controlService = new ServiceDefinition("mockControl", controlServiceContract);
    }

    /**
     * Loads the mock component type.
     *
     * @param mockedInterfaces Interfaces that need to be mocked.
     * @param context          Loader context.
     * @return Mock component type.
     */
    public InjectingComponentType load(List<String> mockedInterfaces, IntrospectionContext context) {

        InjectingComponentType componentType = new InjectingComponentType();

        ClassLoader classLoader = context.getClassLoader();
        for (String mockedInterface : mockedInterfaces) {
            Class<?> interfaceClass;
            try {
                interfaceClass = classLoader.loadClass(mockedInterface);
            } catch (ClassNotFoundException e) {
                MissingResource failure = new MissingResource("Mock interface not found: " + mockedInterface, mockedInterface, componentType);
                context.addError(failure);
                continue;
            }

            ServiceContract serviceContract = contractProcessor.introspect(interfaceClass, context, componentType);

            String name = interfaceClass.getName();
            int index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(index + 1);
            }
            componentType.add(new ServiceDefinition(name, serviceContract));
        }
        componentType.add(controlService);
        componentType.setScope("STATELESS");

        return componentType;
    }

}
