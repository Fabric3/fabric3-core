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
package org.fabric3.fabric.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.annotation.Consumer;
import org.fabric3.api.annotation.Producer;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.implementation.system.introspection.SystemConstructorHeuristic;
import org.fabric3.implementation.system.introspection.SystemHeuristic;
import org.fabric3.implementation.system.introspection.SystemImplementationIntrospectorImpl;
import org.fabric3.implementation.system.introspection.SystemServiceHeuristic;
import org.fabric3.implementation.system.introspection.SystemUnannotatedHeuristic;
import org.fabric3.introspection.java.DefaultClassVisitor;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.introspection.java.ReferenceProcessorImpl;
import org.fabric3.introspection.java.annotation.ConsumerProcessor;
import org.fabric3.introspection.java.annotation.ManagementOperationProcessor;
import org.fabric3.introspection.java.annotation.ManagementProcessor;
import org.fabric3.introspection.java.annotation.OASISDestroyProcessor;
import org.fabric3.introspection.java.annotation.OASISEagerInitProcessor;
import org.fabric3.introspection.java.annotation.OASISInitProcessor;
import org.fabric3.introspection.java.annotation.OASISPropertyProcessor;
import org.fabric3.introspection.java.annotation.OASISReferenceProcessor;
import org.fabric3.introspection.java.annotation.OASISServiceProcessor;
import org.fabric3.introspection.java.annotation.ProducerProcessor;
import org.fabric3.introspection.java.contract.JavaContractProcessorImpl;
import org.fabric3.monitor.introspection.MonitorProcessor;
import org.fabric3.spi.introspection.java.ImplementationIntrospector;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.AnnotationProcessor;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

/**
 * Instantiates an ImplementationProcessor for introspecting system components.
 */
public class BootstrapIntrospectionFactory {

    private BootstrapIntrospectionFactory() {
    }

    /**
     * Returns a new ImplementationProcessor for system components.
     *
     * @return a new ImplementationProcessor for system components
     */
    public static ImplementationIntrospector createSystemImplementationProcessor() {
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        JavaContractProcessor contractProcessor = new JavaContractProcessorImpl(helper);

        Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation>> processors
                = new HashMap<>();

        ReferenceProcessorImpl referenceProcessor = new ReferenceProcessorImpl(contractProcessor, helper);

        // OASIS annotations
        processors.put(Property.class, new OASISPropertyProcessor(helper));
        processors.put(Reference.class, new OASISReferenceProcessor(referenceProcessor));
        processors.put(Service.class, new OASISServiceProcessor(contractProcessor));
        processors.put(EagerInit.class, new OASISEagerInitProcessor());
        processors.put(Init.class, new OASISInitProcessor());
        processors.put(Destroy.class, new OASISDestroyProcessor());

        // F3 annotations
        processors.put(Monitor.class, new MonitorProcessor(helper, contractProcessor));
        processors.put(Producer.class, new ProducerProcessor(contractProcessor, helper));
        processors.put(Consumer.class, new ConsumerProcessor(helper));
        processors.put(Management.class, new ManagementProcessor());
        processors.put(ManagementOperation.class, new ManagementOperationProcessor());

        ClassVisitor classVisitor = new DefaultClassVisitor(processors);

        // heuristics for system components
        SystemServiceHeuristic serviceHeuristic = new SystemServiceHeuristic(contractProcessor, helper);
        SystemConstructorHeuristic constructorHeuristic = new SystemConstructorHeuristic();
        SystemUnannotatedHeuristic unannotatedHeuristic = new SystemUnannotatedHeuristic(helper, contractProcessor);
        SystemHeuristic systemHeuristic = new SystemHeuristic(serviceHeuristic, constructorHeuristic, unannotatedHeuristic);

        return new SystemImplementationIntrospectorImpl(classVisitor, systemHeuristic, helper);
    }

}