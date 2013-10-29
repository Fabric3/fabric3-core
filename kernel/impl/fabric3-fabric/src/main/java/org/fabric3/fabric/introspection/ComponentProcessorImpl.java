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
package org.fabric3.fabric.introspection;

import javax.xml.namespace.QName;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Map;

import org.fabric3.api.annotation.model.Implementation;
import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.processor.ComponentProcessor;
import org.fabric3.spi.introspection.processor.ImplementationProcessor;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class ComponentProcessorImpl implements ComponentProcessor {
    private Map<QName, ImplementationProcessor<?>> implementationProcessors = Collections.emptyMap();

    @Reference(required = false)
    public void setImplementationProcessors(Map<QName, ImplementationProcessor<?>> implementationProcessors) {
        this.implementationProcessors = implementationProcessors;
    }

    @SuppressWarnings("unchecked")
    public void process(ComponentDefinition<?> definition, IntrospectionContext context) {
        QName type = definition.getImplementation().getType();
        ImplementationProcessor processor = implementationProcessors.get(type);
        if (processor == null) {
            context.addError(new UnknownImplementation("Unknown implementation type: " + type));
            return;
        }
        processor.process(definition, context);
    }

    @SuppressWarnings("unchecked")
    public void process(ComponentDefinition<?> definition, Class clazz, IntrospectionContext context) {
        QName implementationType = JavaImplementation.IMPLEMENTATION_JAVA;   // default to Java the implementation type
        for (Annotation annotation : clazz.getAnnotations()) {
            Implementation implementation = annotation.annotationType().getAnnotation(Implementation.class);
            if (implementation != null) {
                implementationType = QName.valueOf(implementation.value());
                break;
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
