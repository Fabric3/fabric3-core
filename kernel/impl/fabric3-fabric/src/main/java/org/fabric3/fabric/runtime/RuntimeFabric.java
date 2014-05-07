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
*/
package org.fabric3.fabric.runtime;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Map;

import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.Fabric3Runtime;
import org.fabric3.api.node.Bootstrap;
import org.fabric3.api.node.Domain;
import org.fabric3.api.node.Fabric;
import org.fabric3.api.node.FabricException;

/**
 * Default Fabric implementation for Fabric3 runtimes.
 */
public class RuntimeFabric implements Fabric {
    private Fabric3Runtime runtime;
    private Domain cached;

    public RuntimeFabric(Fabric3Runtime runtime) {
        this.runtime = runtime;
        // set the bootstrap cache
        try {
            Field field = Bootstrap.class.getDeclaredField("CACHED");
            field.setAccessible(true);
            field.set(null, this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }

    public Fabric addProfile(String name) {
        throw new UnsupportedOperationException();
    }

    public Fabric addProfile(URL location) {
        throw new UnsupportedOperationException();
    }

    public Fabric addExtension(String name) {
        throw new UnsupportedOperationException();
    }

    public Fabric addExtension(URL location) {
        throw new UnsupportedOperationException();
    }

    public Fabric start() throws FabricException {
        throw new UnsupportedOperationException();
    }

    public Fabric stop() throws FabricException {
        throw new UnsupportedOperationException();
    }

    public <T> T createTransportDispatcher(Class<T> interfaze, Map<String, Object> properties) {
        throw new UnsupportedOperationException();
    }

    public <T> Fabric registerSystemService(Class<T> interfaze, T instance) throws FabricException {
        throw new UnsupportedOperationException();
    }

    public Domain getDomain() {
        if (cached == null) {
            cached = runtime.getComponent(Domain.class, Names.NODE_DOMAIN_URI);
        }
        return cached;
    }

}
