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
package org.fabric3.binding.file.introspection;

import java.lang.reflect.AccessibleObject;

import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.binding.file.api.annotation.FileBinding;
import org.fabric3.binding.file.api.annotation.Strategy;
import org.fabric3.binding.file.api.model.FileBindingDefinition;
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

    protected void processService(FileBinding annotation,
                                  AbstractService<?> service,
                                  InjectingComponentType componentType,
                                  Class<?> implClass,
                                  IntrospectionContext context) {
        FileBindingDefinition binding = createDefinition(annotation, implClass, context);
        service.addBinding(binding);
    }

    protected void processReference(FileBinding annotation,
                                    ReferenceDefinition reference,
                                    AccessibleObject object,
                                    Class<?> implClass,
                                    IntrospectionContext context) {
        FileBindingDefinition binding = createDefinition(annotation, implClass, context);
        reference.addBinding(binding);
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

