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
package org.fabric3.security.impl;

import org.fabric3.spi.security.BasicSecuritySubject;

/**
 * Implementations store security information.
 */
public interface SecurityStore {

    /**
     * Looks up a subject based on user name
     *
     * @param username the user name
     * @return the subject or null if not found
     * @throws SecurityStoreException if an error occurs performing the lookup
     */
    BasicSecuritySubject find(String username) throws SecurityStoreException;
}
