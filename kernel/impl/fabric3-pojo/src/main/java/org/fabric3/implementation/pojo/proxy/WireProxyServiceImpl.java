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
package org.fabric3.implementation.pojo.proxy;

import java.net.URI;
import java.util.List;

import org.fabric3.implementation.pojo.spi.proxy.ProxyCreationException;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyService;
import org.fabric3.implementation.pojo.spi.proxy.WireProxyServiceExtension;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.Wire;
import org.oasisopen.sca.ServiceReference;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class WireProxyServiceImpl implements WireProxyService {
    private WireProxyServiceExtension extension;

    @Reference(required = false)
    public void setExtensions(List<WireProxyServiceExtension> extensions) {
        if (extensions.isEmpty()) {
            return;
        }
        if (extensions.size() == 1) {
            extension = extensions.get(0);
        } else {
            if (extension != null && !extension.isDefault()) {
                return;
            }
            for (WireProxyServiceExtension entry : extensions) {
                if (!entry.isDefault()) {
                    extension = entry;
                    return;
                }
            }

        }
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, Wire wire, String callbackUri) throws ProxyCreationException {
        checkExtension();
        return extension.createObjectFactory(interfaze, wire, callbackUri);
    }

    public <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
            throws ProxyCreationException {
        checkExtension();
        return extension.createCallbackObjectFactory(interfaze, multiThreaded, callbackUri, wire);
    }

    public <T> ObjectFactory<?> updateCallbackObjectFactory(ObjectFactory<?> factory, Class<T> interfaze, boolean multiThreaded, URI callbackUri, Wire wire)
            throws ProxyCreationException {
        checkExtension();
        return extension.updateCallbackObjectFactory(factory, interfaze, multiThreaded, callbackUri, wire);
    }

    public <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException {
        if (extension == null) {
            throw new IllegalArgumentException("Channel proxy service extension not installed");
        }
        return extension.cast(target);
    }

    private void checkExtension() throws ProxyCreationException {
        if (extension == null) {
            throw new ProxyCreationException("Channel proxy service extension not installed");
        }
    }

}
