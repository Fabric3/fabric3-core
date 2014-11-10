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
package org.fabric3.implementation.spring.runtime.component;

import javax.security.auth.Subject;

import org.fabric3.api.Fabric3RequestContext;
import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.oasisopen.sca.ServiceReference;

/**
 *
 */
public class SpringRequestContext implements Fabric3RequestContext {
    private String name;

    public SpringRequestContext(String name) {
        this.name = name;
    }

    public Subject getSecuritySubject() {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        return workContext.getSubject().getJaasSubject();
    }

    public SecuritySubject getCurrentSubject() {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        return workContext.getSubject();
    }

    public String getServiceName() {
        return name;
    }

    public <B> ServiceReference<B> getServiceReference() {
        throw new UnsupportedOperationException();
    }

    public <CB> CB getCallback() {
        throw new UnsupportedOperationException();
    }

    public <CB> ServiceReference<CB> getCallbackReference() {
        throw new UnsupportedOperationException();
    }

    public <T> T getHeader(Class<T> type, String name) {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        return workContext.getHeader(type, name);
    }

    public void setHeader(String name, Object value) {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        workContext.setHeader(name, value);
    }

    public void removeHeader(String name) {
        WorkContext workContext = WorkContextCache.getThreadWorkContext();
        workContext.removeHeader(name);
    }
}
