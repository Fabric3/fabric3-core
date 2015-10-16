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
package org.fabric3.binding.rs.model;

import java.lang.annotation.Annotation;

import org.fabric3.api.model.type.component.Resource;

/**
 *
 */
public class ProviderResource extends Resource {
    private String providerName;
    private Class<? extends Annotation> bindingAnnotation;
    private Class<?> providerClass;

    public ProviderResource(String providerName, Class<? extends Annotation> bindingAnnotation, Class<?> providerClass) {
        this.providerName = providerName;
        this.bindingAnnotation = bindingAnnotation;
        this.providerClass = providerClass;
    }

    public String getProviderName() {
        return providerName;
    }

    public Class<? extends Annotation> getBindingAnnotation() {
        return bindingAnnotation;
    }

    public Class<?> getProviderClass() {
        return providerClass;
    }


}
