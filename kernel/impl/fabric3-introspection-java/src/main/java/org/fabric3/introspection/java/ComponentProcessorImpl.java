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
package org.fabric3.introspection.java;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fabric3.api.annotation.model.Implementation;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ComponentAnnotationMapper;
import org.fabric3.spi.introspection.java.ComponentProcessor;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ComponentProcessorImpl implements ComponentProcessor {
    private Map<String, ImplementationProcessor<?>> implementationProcessors = Collections.emptyMap();
    private List<ComponentAnnotationMapper> mappers = new ArrayList<>();

    @Reference(required = false)
    public void setImplementationProcessors(Map<String, ImplementationProcessor<?>> implementationProcessors) {
        this.implementationProcessors = implementationProcessors;
    }

    @Reference(required = false)
    public void setMappers(List<ComponentAnnotationMapper> mappers) {
        this.mappers = mappers;
    }

    @SuppressWarnings("unchecked")
    public void process(ComponentDefinition<?> definition, IntrospectionContext context) {
        String type = definition.getImplementation().getType();
        ImplementationProcessor processor = implementationProcessors.get(type);
        if (processor == null) {
            context.addError(new UnknownImplementation("Unknown implementation type: " + type));
            return;
        }
        processor.process(definition, context);
    }

    @SuppressWarnings("unchecked")
    public void process(ComponentDefinition<?> definition, Class clazz, IntrospectionContext context) {
        String implementationType = "java";   // default to Java the implementation type
        for (Annotation annotation : clazz.getAnnotations()) {
            Implementation implementation = annotation.annotationType().getAnnotation(Implementation.class);
            if (implementation != null) {
                implementationType = implementation.value();
                break;
            } else {
                for (ComponentAnnotationMapper mapper : mappers) {
                    String alias = mapper.getImplementationType(annotation);
                    if (alias != null) {
                        implementationType = alias;
                        break;
                    }
                }
            }
        }

        ImplementationProcessor processor = implementationProcessors.get(implementationType);
        if (processor == null) {
            context.addError(new UnknownImplementation("Unknown implementation type: " + implementationType));
            return;
        }
        processor.process(definition, clazz, context);

    }
}
