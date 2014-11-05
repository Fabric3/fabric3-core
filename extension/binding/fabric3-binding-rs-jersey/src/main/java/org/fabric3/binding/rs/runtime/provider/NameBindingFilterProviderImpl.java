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
package org.fabric3.binding.rs.runtime.provider;

import javax.ws.rs.NameBinding;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@Provider
public class NameBindingFilterProviderImpl implements NameBindingFilterProvider {
    private ProviderRegistry providerRegistry;

    public NameBindingFilterProviderImpl(@Reference ProviderRegistry providerRegistry) {
        this.providerRegistry = providerRegistry;
    }

    public void configure(ResourceInfo resourceInfo, FeatureContext context) {

        Set<Class<? extends Annotation>> namedBindings = new HashSet<>();

        Method method = resourceInfo.getResourceMethod();

        Class<?> clazz = method.getDeclaringClass(); // use method class and not resourceInfo.getResourceClass() since the class is F3ResourceHandler
        addNamedBindings(clazz, namedBindings);

        addNamedBindings(method, namedBindings);

        Set<Object> filters = new HashSet<>();
        for (Class<? extends Annotation> binding : namedBindings) {
            Collection<Object> filtersForBinding = providerRegistry.getNameFilters(binding);
            filters.addAll(filtersForBinding);
        }

        for (Object provider : filters) {
            context.register(provider);
        }
    }

    private void addNamedBindings(AnnotatedElement element, Set<Class<? extends Annotation>> namedBindings) {
        for (Annotation annotation : element.getAnnotations()) {
            Class<? extends Annotation> type = annotation.annotationType();
            if (type.isAnnotationPresent(NameBinding.class)) {
                namedBindings.add(type);
            }
        }
    }
}
