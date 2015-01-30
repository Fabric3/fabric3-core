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
package org.fabric3.security.spring;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;
import org.oasisopen.sca.annotation.EagerInit;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 *
 */
@EagerInit
public class SecurityContextInterceptor implements Interceptor {
    private Interceptor next;

    public Message invoke(Message msg) {
        SecuritySubject subject = msg.getWorkContext().getSubject();
        Authentication old = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (subject != null) {
                Authentication authentication = subject.getDelegate(Authentication.class);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            return next.invoke(msg);
        } finally {
            SecurityContextHolder.getContext().setAuthentication(old);
        }
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Interceptor getNext() {
        return next;
    }
}