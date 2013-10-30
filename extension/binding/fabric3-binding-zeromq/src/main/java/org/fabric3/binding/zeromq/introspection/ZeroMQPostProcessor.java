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
package org.fabric3.binding.zeromq.introspection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.fabric3.api.binding.zeromq.annotation.ZeroMQ;
import org.fabric3.api.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.model.type.ModelObject;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.PostProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class ZeroMQPostProcessor implements PostProcessor {

    public void process(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        processService(componentType, implClass, context);
        processReferences(componentType, implClass, context);
    }

    private void processService(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        ZeroMQ annotation = implClass.getAnnotation(ZeroMQ.class);
        if (annotation == null) {
            return;
        }
        Class<?> serviceInterface = annotation.service();
        AbstractService boundService = null;
        if (serviceInterface == null) {
            if (componentType.getServices().size() != 1) {
                InvalidAnnotation error = new InvalidAnnotation("ZeroMQ binding annotation must specify a service interface", implClass, annotation, implClass);
                context.addError(error);
                return;
            }
            boundService = componentType.getServices().values().iterator().next();
        } else {
            String name = serviceInterface.getName();
            for (AbstractService service : componentType.getServices().values()) {
                String interfaceName = service.getServiceContract().getQualifiedInterfaceName();
                if (interfaceName.equals(name)) {
                    boundService = service;
                    break;
                }
            }
            if (boundService == null) {
                InvalidAnnotation error = new InvalidAnnotation("Service specified in ZeroMQ binding annotation not found: " + name,
                                                                implClass,
                                                                annotation,
                                                                implClass);
                context.addError(error);
                return;
            }
        }
        ZeroMQMetadata metadata = new ZeroMQMetadata();
        String bindingName = "ZMQ" + serviceInterface.getSimpleName();
        ZeroMQBindingDefinition binding = new ZeroMQBindingDefinition(bindingName, metadata);
        boundService.addBinding(binding);
    }

    private void processReferences(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        for (Map.Entry<ModelObject, InjectionSite> entry : componentType.getInjectionSiteMappings().entrySet()) {
            if (!(entry.getKey() instanceof ReferenceDefinition)) {
                continue;
            }
            ReferenceDefinition reference = (ReferenceDefinition) entry.getKey();
            InjectionSite site = entry.getValue();
            if (site instanceof FieldInjectionSite) {
                FieldInjectionSite fieldSite = (FieldInjectionSite) site;
                Field field = fieldSite.getField();
                processAnnotation(field, reference, implClass, context);
            } else if (site instanceof MethodInjectionSite) {
                MethodInjectionSite methodSite = (MethodInjectionSite) site;
                Method method = methodSite.getMethod();
                processAnnotation(method, reference, implClass, context);
            } else if (site instanceof ConstructorInjectionSite) {
                ConstructorInjectionSite constructorSite = (ConstructorInjectionSite) site;
                Constructor<?> constructor = constructorSite.getConstructor();
                processAnnotation(constructor, reference, implClass, context);
            }
        }

    }

    private void processAnnotation(AccessibleObject accessibleObject, ReferenceDefinition reference, Class<?> implClass, IntrospectionContext context) {
        ZeroMQ annotation = accessibleObject.getAnnotation(ZeroMQ.class);
        if (annotation == null) {
            return;
        }

        ZeroMQMetadata metadata = new ZeroMQMetadata();
        String bindingName = "ZMQ" + reference.getName();
        ZeroMQBindingDefinition binding = new ZeroMQBindingDefinition(bindingName, metadata);

        String target = annotation.target();
        try {
            URI targetUri = new URI(target);
            binding.setTargetUri(targetUri);
        } catch (URISyntaxException e) {
            InvalidAnnotation error = new InvalidAnnotation("Invalid target URI specified on ZeroMQ annotation: " + target,
                                                            accessibleObject,
                                                            annotation,
                                                            implClass,
                                                            e);
            context.addError(error);
        }
        reference.addBinding(binding);
    }

}
