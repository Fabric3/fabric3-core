/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.resource.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.annotation.Resource;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;

/**
 * Processes metadata for the {@link Resource{ annotation.
 *
 * @version $Rev$ $Date$
 */
public class JSR250ResourceProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Resource, I> {
    private final IntrospectionHelper helper;
    private final JavaContractProcessor contractProcessor;

    public JSR250ResourceProcessor(@Reference IntrospectionHelper helper, @Reference JavaContractProcessor contractProcessor) {
        super(Resource.class);
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void visitField(Resource annotation, Field field, Class<?> implClass, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(field, annotation.name());
        Type genericType = field.getGenericType();
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        FieldInjectionSite site = new FieldInjectionSite(field);
        String mappedName = annotation.mappedName();
        if (mappedName.length() == 0) {
            // default to the field type simple name
            mappedName = type.getSimpleName();
        }
        ResourceDefinition definition = createResource(name, type, false, mappedName, context);
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Resource annotation, Method method, Class<?> implClass, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(method, annotation.name());
        Type genericType = helper.getGenericType(method);
        TypeMapping typeMapping = context.getTypeMapping(implClass);
        Class<?> type = helper.getBaseType(genericType, typeMapping);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        String mappedName = annotation.mappedName();
        if (mappedName.length() == 0) {
            // default to the field type simple name
            mappedName = type.getSimpleName();
        }
        ResourceDefinition definition = createResource(name, type, false, mappedName, context);
        implementation.getComponentType().add(definition, site);
    }

    private SystemSourcedResource createResource(String name, Class<?> type, boolean optional, String mappedName, IntrospectionContext context) {
        ServiceContract serviceContract = contractProcessor.introspect(type, context);
        return new SystemSourcedResource(name, optional, mappedName, serviceContract);
    }
}
