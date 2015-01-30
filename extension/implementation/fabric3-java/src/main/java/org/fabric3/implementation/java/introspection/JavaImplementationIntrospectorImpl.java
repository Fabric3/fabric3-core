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
package org.fabric3.implementation.java.introspection;

import java.util.Collections;
import java.util.List;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.ImplementationNotFoundException;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidImplementation;
import org.fabric3.spi.introspection.java.PostProcessor;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@Key("java")
public class JavaImplementationIntrospectorImpl implements JavaImplementationIntrospector {
    private final ClassVisitor classVisitor;
    private final HeuristicProcessor heuristic;
    private final IntrospectionHelper helper;
    private List<PostProcessor> postProcessors = Collections.emptyList();

    @Reference(required = false)
    public void setPostProcessors(List<PostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public JavaImplementationIntrospectorImpl(@Reference(name = "classVisitor") ClassVisitor classVisitor,
                                              @Reference(name = "heuristic") HeuristicProcessor heuristic,
                                              @Reference(name = "helper") IntrospectionHelper helper) {
        this.classVisitor = classVisitor;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public void introspect(InjectingComponentType componentType, IntrospectionContext context) {
        String className = componentType.getImplClass();
        componentType.setScope("STATELESS");

        ClassLoader cl = context.getClassLoader();

        Class<?> implClass;
        try {
            implClass = helper.loadClass(className, cl);
        } catch (ImplementationNotFoundException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError) {
                // CNFE and NCDFE may be thrown as a result of a referenced class not being on the classpath
                // If this is the case, ensure the correct class name is reported, not just the implementation 
                context.addError(new ImplementationArtifactNotFound(className, e.getCause().getMessage(), componentType));
            } else {
                context.addError(new ImplementationArtifactNotFound(className, componentType));
            }
            return;
        }
        if (implClass.isInterface()) {
            InvalidImplementation failure = new InvalidImplementation("Implementation class is an interface", implClass, componentType);
            context.addError(failure);
            return;
        }

        TypeMapping mapping = context.getTypeMapping(implClass);
        if (mapping == null) {
            mapping = new TypeMapping();
            context.addTypeMapping(implClass, mapping);
            helper.resolveTypeParameters(implClass, mapping);
        }

        try {
            classVisitor.visit(componentType, implClass, context);
            heuristic.applyHeuristics(componentType, implClass, context);
        } catch (NoClassDefFoundError e) {
            // May be thrown as a result of a referenced class not being on the classpath
            context.addError(new ImplementationArtifactNotFound(className, e.getMessage(), componentType));
        }
        validateScope(componentType, implClass, context);
        for (PostProcessor postProcessor : postProcessors) {
            postProcessor.process(componentType, implClass, context);
        }
    }

    private void validateScope(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        String scope = componentType.getScope();
        if (componentType.isEagerInit() && !Scope.COMPOSITE.getScope().equals(scope) && !Scope.DOMAIN.getScope().equals(scope)) {
            EagerInitNotSupported warning = new EagerInitNotSupported(implClass, componentType);
            context.addWarning(warning);
        }
    }

}
