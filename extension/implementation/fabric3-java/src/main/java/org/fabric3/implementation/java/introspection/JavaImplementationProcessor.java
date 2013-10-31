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
package org.fabric3.implementation.java.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fabric3.api.annotation.Consumer;
import org.fabric3.api.model.type.component.ComponentConsumer;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.component.ConsumerDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.PostProcessor;
import org.fabric3.spi.introspection.java.annotation.AnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Adds metadata for Java component implementations.
 */
@EagerInit
public class JavaImplementationProcessor implements ImplementationProcessor<JavaImplementation> {
    private JavaContractProcessor contractProcessor;
    private JavaImplementationIntrospector introspector;
    private IntrospectionHelper helper;
    private Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation>> annotationProcessors;
    private List<PostProcessor> postProcessors = Collections.emptyList();

    @Reference(required = false)
    public void setPostProcessors(List<PostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    @Reference
    public void setAnnotationProcessors(Map<Class<? extends Annotation>, AnnotationProcessor<? extends Annotation>> processors) {
        this.annotationProcessors = processors;
    }

    public JavaImplementationProcessor(@Reference JavaContractProcessor contractProcessor,
                                       @Reference JavaImplementationIntrospector introspector,
                                       @Reference IntrospectionHelper helper) {
        this.contractProcessor = contractProcessor;
        this.introspector = introspector;
        this.helper = helper;
    }

    public void process(ComponentDefinition<JavaImplementation> definition, IntrospectionContext context) {
        JavaImplementation implementation = definition.getImplementation();
        Object instance = implementation.getInstance();
        InjectingComponentType componentType = implementation.getComponentType();
        if (instance == null) {
            introspector.introspect(componentType, context);
        } else {
            componentType.setScope("COMPOSITE");

            if (componentType.getServices().isEmpty()) {
                // introspect services if not defined
                addServiceDefinitions(instance, componentType, context);
            }
            processAnnotations(instance, definition, context);

            for (PostProcessor postProcessor : postProcessors) {
                postProcessor.process(componentType, instance.getClass(), context);
            }
        }
    }

    public void process(ComponentDefinition<JavaImplementation> definition, Class<?> clazz, IntrospectionContext context) {
        JavaImplementation implementation = new JavaImplementation();
        implementation.setImplementationClass(clazz.getName());
        InjectingComponentType componentType = new InjectingComponentType(clazz.getName());
        implementation.setComponentType(componentType);
        definition.setImplementation(implementation);
        process(definition, context);
    }

    @SuppressWarnings("unchecked")
    private void processAnnotations(Object instance, ComponentDefinition<?> definition, IntrospectionContext context) {
        InjectingComponentType componentType = (InjectingComponentType) definition.getComponentType();
        Class<?> implClass = instance.getClass();
        // handle consumer annotations
        AnnotationProcessor consumerProcessor = annotationProcessors.get(Consumer.class);
        for (Method method : implClass.getDeclaredMethods()) {
            Consumer consumer = method.getAnnotation(Consumer.class);
            if (consumer == null) {
                continue;
            }
            TypeMapping mapping = context.getTypeMapping(implClass);
            if (mapping == null) {
                mapping = new TypeMapping();
                context.addTypeMapping(implClass, mapping);
            }

            helper.resolveTypeParameters(implClass, mapping);

            consumerProcessor.visitMethod(consumer, method, implClass, componentType, context);
        }
        // add automatic configuration for consumer annotations
        for (ConsumerDefinition consumerDefinition : componentType.getConsumers().values()) {
            String name = consumerDefinition.getName();
            URI channelUri = URI.create(name);
            ComponentConsumer componentConsumer = new ComponentConsumer(name, Collections.singletonList(channelUri));
            definition.add(componentConsumer);
        }
    }

    private void addServiceDefinitions(Object instance, InjectingComponentType componentType, IntrospectionContext context) {
        Class<?> implClass = instance.getClass();
        Class[] interfaces = implClass.getInterfaces();
        Class<?> serviceInterface;
        if (interfaces.length == 0) {
            serviceInterface = implClass;
        } else if (interfaces.length == 1) {
            serviceInterface = interfaces[0];
        } else {
            MultipleInterfacesSupported failure = new MultipleInterfacesSupported(implClass, componentType);
            context.addError(failure);
            return;
        }

        String serviceName = serviceInterface.getSimpleName();
        ServiceContract contract = contractProcessor.introspect(serviceInterface, context);
        ServiceDefinition serviceDefinition = new ServiceDefinition(serviceName, contract);
        componentType.add(serviceDefinition);
    }
}
