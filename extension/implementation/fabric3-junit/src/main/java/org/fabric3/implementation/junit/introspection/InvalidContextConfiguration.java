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
package org.fabric3.implementation.junit.introspection;

import javax.xml.stream.Location;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.xml.XmlValidationFailure;

/**
 * Thrown when attempting to load an invalid context configuration for a JUnit implementation.
 */
public class InvalidContextConfiguration extends XmlValidationFailure {

    public InvalidContextConfiguration(String message, Location location, ModelObject modelObject) {
        super(message, location, modelObject);
    }
}
