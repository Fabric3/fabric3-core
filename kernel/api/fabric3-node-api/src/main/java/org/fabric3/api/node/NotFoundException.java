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
package org.fabric3.api.node;

import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Raised when there is an attempt to resolve a non-existent service or channel.
 */
public class NotFoundException extends ServiceRuntimeException {
    private static final long serialVersionUID = -6680741515631722686L;

    public NotFoundException(String message) {
        super(message);
    }
}
