/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.java.model.JavaImplementation;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.Scope;
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

/**
 * @version $Rev$ $Date$
 */
public class JavaHeuristic implements HeuristicProcessor<JavaImplementation> {

    private final IntrospectionHelper helper;
    private final JavaContractProcessor contractProcessor;

    private final HeuristicProcessor<JavaImplementation> serviceHeuristic;
    private PolicyAnnotationProcessor policyProcessor;

    public JavaHeuristic(@Reference IntrospectionHelper helper,
                         @Reference JavaContractProcessor contractProcessor,
                         @Reference(name = "service") HeuristicProcessor<JavaImplementation> serviceHeuristic) {
        this.helper = helper;
        this.contractProcessor = contractProcessor;
        this.serviceHeuristic = serviceHeuristic;
    }

    @Reference
    public void setPolicyProcessor(PolicyAnnotationProcessor processor) {
        this.policyProcessor = processor;
    }

    public void applyHeuristics(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {

        InjectingComponentType componentType = implementation.getComponentType();

        // apply service heuristic
        serviceHeuristic.applyHeuristics(implementation, implClass, context);

        if (componentType.getConstructor() == null) {
            Signature ctor = findConstructor(implClass, context);
            componentType.setConstructor(ctor);
        }

        if (componentType.getProperties().isEmpty() && componentType.getReferences().isEmpty() && componentType.getResourceReferences().isEmpty()) {
            evaluateConstructor(implementation, implClass, context);
            evaluateSetters(implementation, implClass, context);
            evaluateFields(implementation, implClass, context);
        }

        String scope = componentType.getScope();
        if (componentType.isManaged() && !Scope.getScope(scope).isSingleton()) {
            IllegalManagementAttribute warning = new IllegalManagementAttribute(implClass.getName());
            context.addWarning(warning);
        }

    }

    private Signature findConstructor(Class<?> implClass, IntrospectionContext context) {
        Constructor<?>[] constructors = implClass.getDeclaredConstructors();
        Constructor<?> selected = null;
        if (constructors.length == 1) {
            selected = constructors[0];
        } else {
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(org.oasisopen.sca.annotation.Constructor.class)) {
                    if (selected != null) {
                        context.addError(new AmbiguousConstructor(implClass));
                        return null;
                    }
                    selected = constructor;
                }
            }
            if (selected == null) {
                context.addError(new NoConstructorFound(implClass));
                return null;
            }
        }
        return new Signature(selected);
    }

    private void evaluateConstructor(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        InjectingComponentType componentType = implementation.getComponentType();
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        Constructor<?> constructor;
        try {
            Signature ctor = componentType.getConstructor();
            if (ctor == null) {
                // there could have been an error evaluating the constructor, in which case no signature will be present
                return;
            }
            constructor = ctor.getConstructor(implClass);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }

        Type[] parameterTypes = constructor.getGenericParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            InjectionSite site = new ConstructorInjectionSite(constructor, i);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            TypeMapping typeMapping = context.getTypeMapping(implClass);
            Class<?> parameterType = helper.getBaseType(parameterTypes[i], typeMapping);

            String name = helper.getSiteName(constructor, i, null);
            Annotation[] annotations = constructor.getParameterAnnotations()[i];
            processSite(componentType, name, parameterType, implClass, site, annotations, context);
        }
    }

    private void evaluateSetters(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        InjectingComponentType componentType = implementation.getComponentType();
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        Set<Method> setters = helper.getInjectionMethods(implClass, componentType.getServices().values());
        for (Method setter : setters) {
            InjectionSite site = new MethodInjectionSite(setter, 0);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(setter, null);
            TypeMapping typeMapping = context.getTypeMapping(implClass);
            Type genericType = setter.getGenericParameterTypes()[0];
            Class<?> parameterType = helper.getBaseType(genericType, typeMapping);
            Annotation[] annotations = setter.getAnnotations();
            processSite(componentType, name, parameterType, implClass, site, annotations, context);
        }
    }

    private void evaluateFields(JavaImplementation implementation, Class<?> implClass, IntrospectionContext context) {
        InjectingComponentType componentType = implementation.getComponentType();
        Map<InjectionSite, Injectable> sites = componentType.getInjectionSites();
        Set<Field> fields = helper.getInjectionFields(implClass);
        for (Field field : fields) {
            InjectionSite site = new FieldInjectionSite(field);

            // skip sites that have already been mapped
            if (sites.containsKey(site)) {
                continue;
            }

            String name = helper.getSiteName(field, null);
            TypeMapping typeMapping = context.getTypeMapping(implClass);
            Class<?> parameterType = helper.getBaseType(field.getGenericType(), typeMapping);
            Annotation[] annotations = field.getAnnotations();
            processSite(componentType, name, parameterType, implClass, site, annotations, context);
        }
    }


    private void processSite(InjectingComponentType componentType,
                             String name,
                             Class<?> parameterType,
                             Class<?> declaringClass,
                             InjectionSite site,
                             Annotation[] annotations,
                             IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(declaringClass);
        InjectableType type = helper.inferType(parameterType, typeMapping);
        switch (type) {
        case PROPERTY:
            addProperty(componentType, name, parameterType, declaringClass, site, context);
            break;
        case REFERENCE:
            addReference(componentType, name, parameterType, declaringClass, site, annotations, context);
            break;
        case CALLBACK:
            context.addError(new UnknownInjectionType(site, type, componentType.getImplClass()));
            break;
        default:
            context.addError(new UnknownInjectionType(site, type, componentType.getImplClass()));
        }
    }

    private void addProperty(InjectingComponentType componentType,
                             String name,
                             Type type,
                             Class<?> declaringClass,
                             InjectionSite site,
                             IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(declaringClass);
        Property property = new Property(name);
        MultiplicityType multiplicityType = helper.introspectMultiplicity(type, typeMapping);
        property.setMany(MultiplicityType.COLLECTION == multiplicityType || MultiplicityType.DICTIONARY == multiplicityType);
        componentType.add(property, site);
    }

    @SuppressWarnings({"unchecked"})
    private void addReference(InjectingComponentType componentType,
                              String name,
                              Class<?> parameterType,
                              Class<?> declaringClass,
                              InjectionSite site,
                              Annotation[] annotations,
                              IntrospectionContext context) {
        TypeMapping typeMapping = context.getTypeMapping(declaringClass);
        ServiceContract contract = contractProcessor.introspect(parameterType, context);
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
