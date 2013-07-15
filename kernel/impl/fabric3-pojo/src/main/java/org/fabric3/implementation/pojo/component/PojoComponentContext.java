/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.component;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import org.fabric3.api.Fabric3ComponentContext;
import org.fabric3.host.runtime.HostInfo;
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