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

import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.binding.file.model.FileBinding;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.AbstractBindingPostProcessor;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Introspects file binding information in a component implementation.
 */
@EagerInit
public class FileBindingPostProcessor extends AbstractBindingPostProcessor<org.fabric3.api.binding.file.annotation.FileBinding> {

    public FileBindingPostProcessor() {
        super(org.fabric3.api.binding.file.annotation.FileBinding.class);
    }

    protected Binding processReference(org.fabric3.api.binding.file.annotation.FileBinding annotation,
                                       Reference reference,
                                       Class<?> implClass,
                                       IntrospectionContext context) {
        return createBinding(annotation, implClass, context);
    }

    protected Binding processService(org.fabric3.api.binding.file.annotation.FileBinding annotation,
                                     Service<ComponentType> service,
                                     InjectingComponentType componentType,
                                     Class<?> implClass,
                                     IntrospectionContext context) {
        return createBinding(annotation, implClass, context);

    }

    protected Binding processServiceCallback(org.fabric3.api.binding.file.annotation.FileBinding annotation,
                                             Service<ComponentType> service,
                                             InjectingComponentType componentType,
                                             Class<?> implClass,
                                             IntrospectionContext context) {
        return null; // not supported
    }

    protected Binding processReferenceCallback(org.fabric3.api.binding.file.annotation.FileBinding annotation,
                                               Reference reference,
                                               Class<?> implClass,
                                               IntrospectionContext context) {
        return null; // not supported
    }

    private FileBinding createBinding(org.fabric3.api.binding.file.annotation.FileBinding annotation, Class<?> implClass, IntrospectionContext context) {
        if(!isActiveForEnvironment(annotation.environments())) {
            return null;
        }
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
        String archiveLocation = getNullableValue(annotation.archiveLocation());
        if (strategy == Strategy.ARCHIVE && archiveLocation == null) {
            InvalidAnnotation error = new InvalidAnnotation("File binding annotation must specify an archive location", implClass, annotation, implClass);
            context.addError(error);
        }
        String errorLocation = getNullableValue(annotation.errorLocation());
        String adapterUri = getNullableValue(annotation.adaptor());
        String pattern = getNullableValue(annotation.pattern());
        long delay = annotation.delay();
        if (delay < -1) {
            InvalidAnnotation error = new InvalidAnnotation("Invalid delay value specified on file binding", implClass, annotation, implClass);
            context.addError(error);
        }
        return new FileBinding(name, pattern, location, strategy, archiveLocation, errorLocation, null, adapterUri, delay);

    }

}

