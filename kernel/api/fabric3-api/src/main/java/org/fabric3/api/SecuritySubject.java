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
package org.fabric3.api;

import javax.security.auth.Subject;
import java.util.Set;

/**
 * Represents user security state. Implementations wrap a subject supplied by the underlying security provider.
 */
public interface SecuritySubject {

    /**
     * Returns the user name of this subject.
     *
     * @return the user name of this subject.
     */
    String getUsername();

    /**
     * Returns the assigned roles of this subject
     *
     * @return the roles
     */
    Set<Role> getRoles();

    /**
     * Returns the underlying security provider subject.
     *
     * @param type the subject type
     * @return the security provider subject
     */
    <T> T getDelegate(Class<T> type);

    /**
     * Returns the JAAS representation of this subject. Note inheritance cannot be used as the JAAS Subject is a final class.
     *
     * @return the JAAS representation of this subject
     */
    Subject getJaasSubject();
}
