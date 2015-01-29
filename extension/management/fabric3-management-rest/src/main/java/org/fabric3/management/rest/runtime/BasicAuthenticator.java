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
package org.fabric3.management.rest.runtime;

import javax.servlet.http.HttpServletRequest;

import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.NoCredentialsException;

/**
 * Handles HTTP basic auth.
 */
public interface BasicAuthenticator {

    /**
     * Perform the authentication. If successful, the work context will be updated with the authenticated security subject.
     *
     * @param request the current HTTP request
     * @param context the current work context
     * @throws NoCredentialsException  if authentication credentials were not supplied. Clients may be asked to supply credentials and retry.
     * @throws AuthenticationException if authentication failed
     */
    void authenticate(HttpServletRequest request, WorkContext context) throws NoCredentialsException, AuthenticationException;

}
