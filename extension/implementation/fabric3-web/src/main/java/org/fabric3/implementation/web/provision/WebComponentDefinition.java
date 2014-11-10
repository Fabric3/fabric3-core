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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.web.provision;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.api.model.type.java.InjectionSite;

/**
 *
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class WebComponentDefinition extends PhysicalComponentDefinition {
    private static final long serialVersionUID = 2871569095506575868L;
    private String contextUrl;
    // map of resource id to injection site name/InjectionSite pair
    private Map<String, Map<String, InjectionSite>> injectionSiteMappings = new HashMap<>();
    private final Map<String, Document> propertyValues = new HashMap<>();

    public Map<String, Map<String, InjectionSite>> getInjectionSiteMappings() {
        return injectionSiteMappings;
    }

    public void setInjectionMappings(Map<String, Map<String, InjectionSite>> mappings) {
        injectionSiteMappings = mappings;
    }

    public String getContextUrl() {
        return contextUrl;
    }

    public void setContextUrl(String contextUrl) {
        this.contextUrl = contextUrl;
    }

    public Map<String, Document> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValue(String name, Document value) {
        propertyValues.put(name, value);
    }

}
