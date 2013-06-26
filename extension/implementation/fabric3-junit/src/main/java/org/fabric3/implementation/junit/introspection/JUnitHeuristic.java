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
package org.fabric3.implementation.junit.introspection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.MultiplicityType;
import org.fabric3.spi.introspection.java.NoConstructorFound;
import org.fabric3.spi.introspection.java.UnknownInjectionType;
import org.fabric3.spi.introspection.java.annotation.AmbiguousConstructor;
import org.fabric3.spi.introspection.java.annotation.PolicyAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.fabric3.spi.model.type.java.ConstructorInjectionSite;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.model.type.java.InjectableType;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.InjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.spi.model.type.java.Signature;
import org.junit.After;
import org.junit.Before;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class JUnitHeuristic implements HeuristicProcessor {
    private IntrospectionHelper helper;
    private JavaContractProcessor contractProcessor;

    private HeuristicProcessor serviceHeuristic;
    private PolicyAnnotationProcessor policyProcessor;

    public JUnitHeuristic(@Reference IntrospectionHelper helper,
                          @Reference JavaContractProcessor contractProcessor,
                          @Reference(name = "service") HeuristicProcessor serviceHeuristic) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        this.serviceHeuristic = serviceHeuristic;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public void applyHeuristics(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {

        serviceHeuristic.applyHeuristics(componentType, implClass, context);

        if (componentType.getConstructor() == null) {
            Signature ctor = findConstructor(implClass, componentType, context);
            componentType.setConstructor(ctor);
        }

        if (componentType.getProperties().isEmpty() && componentType.getReferences().isEmpty() && componentType.getResourceReferences().isEmpty()) {
            evaluateConstructor(componentType, implClass, context);
            evaluateSetters(componentType, implClass, context);
            evaluateFields(componentType, implClass, context);
        }

        if (componentType.getInitMethod() == null) {
            Signature setUp = getCallback(implClass, "setUp");
            if (setUp == null) {
                setUp = getCallback(implClass, Before.class);
            }
            componentType.setInitMethod(setUp);
        }
        if (componentType.getDestroyMethod() == null) {
            Signature tearDown = getCallback(implClass, "tearDown");
            if (tearDown == null) {
                tearDown = getCallback(implClass, After.class);
            }
            componentType.setDestroyMethod(tearDown);
        }
    }

    private Signature getCallback(Class<?> implClass, String name) {
        while (Object.class != implClass) {
            try {
                Method callback = implClass.getDeclaredMethod(name);
                return new Signature(callback);
            } catch (NoSuchMethodException e) {
                implClass = implClass.getSuperclass();
            }
        }
        return null;
    }

    private Signature getCallback(Class<?> implClass, Class<? extends Annotation> annotation) {
        while (Object.class != implClass) {
            for (Method method : implClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(annotation)) {
                    return new Signature(method);
                }
            }
        }
        return null;
    }

    private Signature findConstructor(Class<?> implClass, InjectingComponentType componentType, IntrospectionContext context) {
        Constructor<?>[] constructors = implClass.getDeclaredConstructors();
        Constructor<?> selected = null;
        if (constructors.length == 1) {
            selected = constructors[0];
        } else {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(org.oasisopen.sca.annotation.Constructor.class)) {
                    if (selected != null) {
                        context.addError(new AmbiguousConstructor(implClass, componentType));
                        return null;
                    }
                    selected = constructor;
                }
            }
            if (selected == null) {
                context.addError(new NoConstructorFound(implClass, componentType));
                return null;
            }
        }
        return new Signature(selected);
    }

    private void evaluateConstructor(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        Constructor<?> constructor;
        try {
            if (componentType.getConstructor() == null) {
                // there was an error with the constructor previously, just return
                return;
            }
            constructor = componentType.getConstructor().getConstructor(implClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }

        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Type[] parameterTypes = constructor.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            InjectionSite site = new ConstructorInjectionSite(constructor, i);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            Type parameterType = parameterTypes[i];
            String name = helper.getSiteName(constructor, i, null);
            Annotation[] annotations = constructor.getParameterAnnotations()[i];
            processSite(componentType, typeMapping, name, constructor, parameterType, site, annotations, context);
        }
    }

    void evaluateSetters(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Set<Method> setters = helper.getInjectionMethods(implClass, componentType.getServices().values());
        for (Method setter : setters) {
            InjectionSite site = new MethodInjectionSite(setter, 0);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(setter, null);
            Type parameterType = setter.getGenericParameterTypes()[0];
            Annotation[] annotations = setter.getAnnotations();
            processSite(componentType, typeMapping, name, setter, parameterType, site, annotations, context);
        }
    }

    void evaluateFields(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Set<Field> fields = helper.getInjectionFields(implClass);
        for (Field field : fields) {
            InjectionSite site = new FieldInjectionSite(field);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(field, null);
            Type parameterType = field.getGenericType();
            Annotation[] annotations = field.getAnnotations();
            processSite(componentType, typeMapping, name, field, parameterType, site, annotations, context);
        }
    }

    private void processSite(InjectingComponentType componentType,
                             TypeMapping typeMapping,
                             String name,
                             Member member,
                             Type parameterType,
                             InjectionSite site,
                             Annotation[] annotations,
                             IntrospectionContext context) {
        InjectableType type = helper.inferType(parameterType, typeMapping);
        switch (type) {
            case PROPERTY:
                addProperty(componentType, typeMapping, name, parameterType, site);
                break;
            case REFERENCE:
                addReference(componentType, typeMapping, name, parameterType, site, annotations, context);
                break;
            case CALLBACK:
                // ignore
                break;
            default:
                context.addError(new UnknownInjectionType(site, type, componentType.getImplClass(), member, componentType));
                break;
        }
    }

    private void addProperty(InjectingComponentType componentType, TypeMapping typeMapping, String name, Type type, InjectionSite site) {
        Property property = new Property(name);
        MultiplicityType multiplicityType = helper.introspectMultiplicity(type, typeMapping);
        property.setMany(MultiplicityType.COLLECTION == multiplicityType || MultiplicityType.DICTIONARY == multiplicityType);
        componentType.add(property, site);
    }

    private void addReference(InjectingComponentType componentType,
                              TypeMapping typeMapping,
                              String name,
                              Type parameterType,
                              InjectionSite site,
                              Annotation[] annotations,
                              IntrospectionContext context) {
        Class<?> type = helper.getBaseType(parameterType, typeMapping);
        ServiceContract contract = contractProcessor.introspect(type, context, componentType);
        ReferenceDefinition reference = new ReferenceDefinition(name, contract);
        helper.processMultiplicity(reference, false, parameterType, typeMapping);
        if (policyProcessor != null) {
            for (Annotation annotation : annotations) {
                policyProcessor.process(annotation, reference, context);
            }
        }
        componentType.add(reference, site);
    }
}
