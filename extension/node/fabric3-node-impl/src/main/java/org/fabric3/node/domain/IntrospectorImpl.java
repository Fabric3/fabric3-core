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
package org.fabric3.node.domain;

import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class IntrospectorImpl implements Introspector {
    private JavaContractProcessor contractProcessor;

    public IntrospectorImpl(@Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    public <T> JavaServiceContract introspect(Class<T> interfaze) throws ContainerException {
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        JavaServiceContract contract = contractProcessor.introspect(interfaze, context);
        StringBuilder builder = new StringBuilder();
        if (context.hasErrors()) {
            for (ValidationFailure failure : context.getErrors()) {
                builder.append(failure.getMessage()).append("\n");
            }
            throw new ContainerException(builder.toString());
        }
        return contract;
    }

}
