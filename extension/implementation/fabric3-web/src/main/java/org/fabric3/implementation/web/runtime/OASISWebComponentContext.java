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
package org.fabric3.implementation.web.runtime;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.fabric3.api.Fabric3ComponentContext;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.container.web.spi.WebRequestTunnel;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.ServiceRuntimeException;

/**
 * Implementation of ComponentContext for Web components.
 */
public class OASISWebComponentContext implements Fabric3ComponentContext {
    private final WebComponent component;
    private HostInfo info;

    public OASISWebComponentContext(WebComponent component, HostInfo info) {
        this.component = component;
        this.info = info;
    }

    public String getURI() {
        try {
            return component.getUri().toString();
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> B getService(Class<B> interfaze, String referenceName) {
        try {
            return interfaze.cast(getSession().getAttribute(referenceName));
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public <B> ServiceReference<B> getServiceReference(Class<B> interfaze, String referenceName) {
        try {
            return ServiceReference.class.cast(getSession().getAttribute(referenceName));
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> Collection<B> getServices(Class<B> interfaze, String referenceName) {
        throw new UnsupportedOperationException();
    }

    public <B> Collection<ServiceReference<B>> getServiceReferences(Class<B> interfaze, String referenceName) {
        throw new UnsupportedOperationException();
    }

    public <B> B getProperty(Class<B> type, String propertyName) {
        try {
            return component.getProperty(type, propertyName);
        } catch (Fabric3Exception e) {
            throw new ServiceRuntimeException(e.getMessage(), e);
        }
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        return null;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        return null;
    }

    public RequestContext getRequestContext() {
        return null;
    }

    public String getRuntimeName() {
        return info.getRuntimeName();
    }

    public URI getDomain() {
        return info.getDomain();
    }

    public String getEnvironment() {
        return info.getEnvironment();
    }

    public File getDataDirectory() {
        return info.getDataDir();
    }

    public File getTempDirectory() {
        return info.getTempDir();
    }

    private HttpSession getSession() {
        HttpServletRequest request = WebRequestTunnel.getRequest();
        if (request == null) {
            throw new ServiceRuntimeException("HTTP request not bound. Check filter configuration.");
        }
        return request.getSession(true);  // force creation of session
    }

    public <B, R extends ServiceReference<B>> R cast(B target) {
        throw new UnsupportedOperationException();
    }

}