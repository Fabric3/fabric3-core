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
package org.fabric3.binding.file.introspection;

import java.lang.reflect.AccessibleObject;

import org.fabric3.api.binding.file.annotation.FileBinding;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.binding.file.model.FileBindingDefinition;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.AbstractBindingPostProcessor;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Introspects file binding information in a component implementation.
 */
@EagerInit
public class FileBindingPostProcessor extends AbstractBindingPostProcessor<FileBinding> {

    public FileBindingPostProcessor() {
        super(FileBinding.class);
    }

    protected BindingDefinition processService(FileBinding annotation,
                                               ServiceDefinition<ComponentType> service,
                                               InjectingComponentType componentType,
                                               Class<?> implClass,
                                               IntrospectionContext context) {
        return createDefinition(annotation, implClass, context);

    }

    protected BindingDefinition processServiceCallback(FileBinding annotation,
                                                       ServiceDefinition<ComponentType> service,
                                                       InjectingComponentType componentType,
                                                       Class<?> implClass,
                                                       IntrospectionContext context) {
        return null; // not supported
    }

    protected BindingDefinition processReference(FileBinding annotation,
                                                 ReferenceDefinition reference,
                                                 AccessibleObject object,
                                                 Class<?> implClass,
                                                 IntrospectionContext context) {
        return createDefinition(annotation, implClass, context);
    }

    protected BindingDefinition processReferenceCallback(FileBinding annotation,
                                                         ReferenceDefinition reference,
                                                         AccessibleObject object,
                                                         Class<?> implClass,
                                                         IntrospectionContext context) {
        return null; // not supported
    }

    private FileBindingDefinition createDefinition(FileBinding annotation, Class<?> implClass, IntrospectionContext context) {
        String name = annotation.name();
        if (name.isEmpty()) {
            name = "FileBinding";
        }
        String location = annotation.location();
        if (location.isEmpty()) {
            InvalidAnnotation error = new InvalidAnnotation("File binding annotation must specify a location", implClass, annotation, implClass);
            context.addError(error);
        }
        Strategy strategy = annotation.strategy();
        String archiveLocation = getNullibleValue(annotation.archiveLocation());
        if (strategy == Strategy.ARCHIVE && archiveLocation == null) {
            InvalidAnnotation error = new InvalidAnnotation("File binding annotation must specify an archive location", implClass, annotation, implClass);
            context.addError(error);
        }
        String errorLocation = getNullibleValue(annotation.errorLocation());
        String adapterUri = getNullibleValue(annotation.adaptor());
        String pattern = getNullibleValue(annotation.pattern());
        long delay = annotation.delay();
        if (delay < -1) {
            InvalidAnnotation error = new InvalidAnnotation("Invalid delay value specified on file binding", implClass, annotation, implClass);
            context.addError(error);
        }
        return new FileBindingDefinition(name, pattern, location, strategy, archiveLocation, errorLocation, null, adapterUri, delay);

    }

}

