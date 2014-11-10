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
package org.fabric3.execution.introspection;

import java.lang.reflect.Member;
import java.util.concurrent.ExecutorService;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.Resource;
import org.fabric3.execution.model.ExecutorServiceResourceReference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.resource.spi.ResourceTypeHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Handles resource injection for the runtime <code>ExecutorService</code>.
 */
@EagerInit
public class ExecutorServiceTypeHandler implements ResourceTypeHandler {
    private ServiceContract contract;
    private JavaContractProcessor contractProcessor;

    public ExecutorServiceTypeHandler(@Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    @Init
    public void init() {
        // introspect the interface once
        contract = contractProcessor.introspect(ExecutorService.class, new DefaultIntrospectionContext());
    }

    public ExecutorServiceResourceReference createResourceReference(String resourceName,
                                                                    Resource annotation,
                                                                    Member member,
                                                                    InjectingComponentType componentType, IntrospectionContext context) {
        return new ExecutorServiceResourceReference(resourceName, contract);
    }
}