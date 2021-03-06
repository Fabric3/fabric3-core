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
package org.fabric3.resource.spi;

import java.lang.reflect.Member;

import org.fabric3.api.annotation.Resource;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Implementations extend resource processing by handling <code>@Resource</code> annotations for specific types such as DataSources.
 */
public interface ResourceTypeHandler {

    /**
     * Creates a {@link ResourceReference} for a given type.
     *
     * @param name  the name of the resource injection site
     * @param annotation    the resource annotation
     * @param member        the Field, Constructor, or Method where the resource is injected
     * @param type the component type being introspected
     * @param context       the current introspection context  @return the ResourceDefinition
     * @return the resource definition
     */
    ResourceReference createResourceReference(String name, Resource annotation, Member member, InjectingComponentType type, IntrospectionContext context);
}
