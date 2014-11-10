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
package org.fabric3.implementation.pojo.component;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.fabric3.api.Fabric3ComponentContext;
import org.fabric3.api.host.runtime.HostInfo;
import org.oasisopen.sca.RequestContext;
import org.oasisopen.sca.ServiceReference;

/**
 *
 */
public class PojoComponentContext implements Fabric3ComponentContext {
    private final PojoComponent component;
    private final PojoRequestContext requestContext;
    private HostInfo info;

    public PojoComponentContext(PojoComponent component, PojoRequestContext requestContext, HostInfo info) {
        this.component = component;
        this.requestContext = requestContext;
        this.info = info;
    }

    public String getURI() {
        return component.getUri().toString();
    }

    public <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException {
        return null;
    }

    public <B> B getService(Class<B> businessInterface, String referenceName) {
        return null;
    }

    public <B> ServiceReference<B> getServiceReference(Class<B> businessInterface, String referenceName) {
        return null;
    }

    public <B> Collection<B> getServices(Class<B> interfaze, String referenceName) {
        return null;
    }

    public <B> Collection<ServiceReference<B>> getServiceReferences(Class<B> interfaze, String referenceName) {
        return null;
    }

    public <B> B getProperty(Class<B> type, String propertyName) {
        return null;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface) {
        return null;
    }

    public <B> ServiceReference<B> createSelfReference(Class<B> businessInterface, String serviceName) {
        return null;
    }

    public RequestContext getRequestContext() {
        return requestContext;
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
}