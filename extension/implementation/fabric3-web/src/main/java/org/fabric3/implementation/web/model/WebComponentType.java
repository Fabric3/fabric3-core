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
package org.fabric3.implementation.web.model;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectionSite;

/**
 * A component type representing a web component.
 */
public class WebComponentType extends ComponentType {
    private final Map<String, Map<InjectionSite, Injectable>> sites = new HashMap<>();

    /**
     * Returns a mapping from artifact id (e.g. servlet or filter class name, servlet context, session context) to injection site/injectable attribute pair
     *
     * @return the mapping
     */
    public Map<String, Map<InjectionSite, Injectable>> getInjectionSites() {
        return sites;
    }

    /**
     * Sets a mapping from artifact id to injection site/injectable attribute pair.
     *
     * @param artifactId the artifact id
     * @param site       the injection site
     * @param attribute  the injectable attribute
     */
    public void addMapping(String artifactId, InjectionSite site, Injectable attribute) {
        Map<InjectionSite, Injectable> mapping = sites.get(artifactId);
        if (mapping == null) {
            mapping = new HashMap<>();
            sites.put(artifactId, mapping);
        }
        mapping.put(site, attribute);
    }

}
