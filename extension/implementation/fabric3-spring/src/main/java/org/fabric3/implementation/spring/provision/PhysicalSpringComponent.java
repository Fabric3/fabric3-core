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
package org.fabric3.implementation.spring.provision;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.model.physical.PhysicalComponent;

/**
 * A physical definition for a Spring-based component.
 */
public class PhysicalSpringComponent extends PhysicalComponent {

    public enum LocationType {
        JAR, DIRECTORY, FILE
    }

    private String baseLocation;
    private List<String> contextLocations;
    private Map<String, String> defaultReferenceMappings;
    private LocationType locationType = LocationType.FILE;

    /**
     * Constructor.
     *
     * @param uri              the component URI
     * @param baseLocation     the base relative application context location if it is contained in a jar, otherwise null
     * @param contextLocations the locations of application context files for the component
     * @param mappings         bean alias mappings derived from the default attribute of SCA reference tags in an application context
     * @param locationType     the application context location type
     */
    public PhysicalSpringComponent(URI uri, String baseLocation, List<String> contextLocations, Map<String, String> mappings, LocationType locationType) {
        this.contextLocations = contextLocations;
        this.locationType = locationType;
        setComponentUri(uri);
        this.baseLocation = baseLocation;
        this.defaultReferenceMappings = mappings;
    }

    public String getBaseLocation() {
        return baseLocation;
    }

    public List<String> getContextLocations() {
        return contextLocations;
    }

    public Map<String, String> getDefaultReferenceMappings() {
        return defaultReferenceMappings;
    }

    public LocationType getLocationType() {
        return locationType;
    }

}