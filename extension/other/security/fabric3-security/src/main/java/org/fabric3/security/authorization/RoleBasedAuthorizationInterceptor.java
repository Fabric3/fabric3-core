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
package org.fabric3.security.authorization;

import java.util.List;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.security.AuthorizationException;
import org.fabric3.spi.security.AuthorizationService;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Interceptor for performing role-based authorization.
 */
public class RoleBasedAuthorizationInterceptor implements Interceptor {
    private Interceptor next;
    private final List<String> roles;
    private final AuthorizationService authorizationService;

    public RoleBasedAuthorizationInterceptor(List<String> roles, AuthorizationService authorizationService) {
        this.roles = roles;
        this.authorizationService = authorizationService;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Message invoke(Message msg) {
        WorkContext workContext = msg.getWorkContext();
        try {
            SecuritySubject subject = workContext.getSubject();
            if (subject == null) {
                msg.setBodyWithFault(new ServiceRuntimeException("Subject not authenticated"));
                return msg;
            }
            authorizationService.checkRoles(subject, roles);
            return next.invoke(msg);
        } catch (AuthorizationException e) {
            msg.setBodyWithFault(new ServiceRuntimeException(e));
            return msg;
        }
    }

}
