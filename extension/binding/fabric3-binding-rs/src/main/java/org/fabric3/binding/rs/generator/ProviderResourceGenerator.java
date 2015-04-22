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
package org.fabric3.binding.rs.generator;

import java.lang.annotation.Annotation;
import java.net.URI;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.binding.rs.model.ProviderResource;
import org.fabric3.binding.rs.provision.PhysicalProviderResource;
import org.fabric3.spi.domain.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.physical.PhysicalResource;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
@Key("org.fabric3.binding.rs.model.ProviderResource")
public class ProviderResourceGenerator implements ResourceGenerator<ProviderResource> {

    public PhysicalResource generateResource(LogicalResource<ProviderResource> resource) {
        ProviderResource providerResource = resource.getDefinition();
        String providerName = providerResource.getProviderName();
        URI filterUri = URI.create(resource.getParent().getUri().toString() + "/" + providerName);
        Class<? extends Annotation> bindingAnnotation = providerResource.getBindingAnnotation();
        Class<?> providerClass = providerResource.getProviderClass();
        return new PhysicalProviderResource(filterUri, bindingAnnotation, providerClass);
    }
}
