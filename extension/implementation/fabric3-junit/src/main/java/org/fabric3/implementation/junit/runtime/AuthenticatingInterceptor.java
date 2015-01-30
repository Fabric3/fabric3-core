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
package org.fabric3.implementation.junit.runtime;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.security.UsernamePasswordToken;
import org.oasisopen.sca.ServiceUnavailableException;

/**
 * Sets a security subject for the current testcase invocation using configuration specified in the JUnit component definition.
 */
public class AuthenticatingInterceptor implements Interceptor {
    private Interceptor next;
    private String username;
    private String password;
    private AuthenticationService authenticationService;

    public AuthenticatingInterceptor(String username, String password, AuthenticationService authenticationService, Interceptor next) {
        this.username = username;
        this.password = password;
        this.authenticationService = authenticationService;
        this.next = next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message msg) {
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        try {
            SecuritySubject subject = authenticationService.authenticate(token);
            msg.getWorkContext().setSubject(subject);
        } catch (AuthenticationException e) {
            throw new ServiceUnavailableException("Error authenticating", e);
        }
        return next.invoke(msg);
    }

}
