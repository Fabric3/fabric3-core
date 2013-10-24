/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.runtime.bootstrap;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

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
                = new HashMap<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation>>();

        // OASIS annotations
        processors.put(Property.class, new OASISPropertyProcessor(helper));
        processors.put(Reference.class, new OASISReferenceProcessor(contractProcessor, helper));
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