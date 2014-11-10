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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.component;

import org.fabric3.spi.container.component.RegistrationException;

/**
 * Denotes an attempt to register a component when one is already regsitered with that id.
 */
public class DuplicateComponentException extends RegistrationException {
    private static final long serialVersionUID = 2257483559370700093L;

    /**
     * Constructor specifying the id of the component.
     *
     * @param message the id of the component, also the default exception message
     */
    public DuplicateComponentException(String message) {
        super(message);
    }

}
