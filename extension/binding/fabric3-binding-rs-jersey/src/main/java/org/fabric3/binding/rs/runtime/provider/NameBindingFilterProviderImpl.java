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
