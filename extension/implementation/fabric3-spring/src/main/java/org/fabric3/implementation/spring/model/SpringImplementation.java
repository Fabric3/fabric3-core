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
package org.fabric3.implementation.spring.model;

import java.util.List;

import org.fabric3.api.model.type.component.Implementation;

/**
 * A Spring component implementation type.
 */
public class SpringImplementation extends Implementation<SpringComponentType> {
    public enum LocationType {
        JAR, DIRECTORY, FILE
    }

    private String location;
    private List<String> contextLocations;

    private LocationType locationType = LocationType.FILE;

    public String getType() {
        return "spring";
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getContextLocations() {
        return contextLocations;
    }

    public void setContextLocations(List<String> locations) {
        this.contextLocations = locations;
    }


    public LocationType getLocationType() {
        return locationType;
    }

    public void setLocationType(LocationType locationType) {
        this.locationType = locationType;
    }
}