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
