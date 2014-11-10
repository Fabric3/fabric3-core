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
package org.fabric3.binding.web.runtime.common;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.Broadcaster;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of the BroadcasterManager.
 */
@EagerInit
public class BroadcasterManagerImpl implements BroadcasterManager {
    private static final JsonType JSON_TYPE = new JsonType(String.class);

    private TransformerRegistry registry;
    private Transformer<Object, String> jsonTransformer;
    private Map<String, Broadcaster> broadcasters = new ConcurrentHashMap<>();

    public BroadcasterManagerImpl(@Reference TransformerRegistry registry) {
        this.registry = registry;
    }

    public Broadcaster getChannelBroadcaster(String path, AtmosphereConfig config) {
        Broadcaster broadcaster = broadcasters.get(path);
        if (broadcaster == null) {
            initializeTransformer();
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                broadcaster = new ChannelBroadcaster(path, jsonTransformer, config);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
            broadcasters.put(path, broadcaster);
        }
        return broadcaster;
    }

    public Broadcaster getServiceBroadcaster(String path, AtmosphereConfig config) {
        Broadcaster broadcaster = broadcasters.get(path);
        if (broadcaster == null) {
            initializeTransformer();
            broadcaster = new ServiceBroadcaster(path, config);
            broadcasters.put(path, broadcaster);
        }
        return broadcaster;
    }

    public void remove(String path) {
        broadcasters.remove(path);
    }

    @SuppressWarnings({"unchecked"})
    public void initializeTransformer() {
        if (jsonTransformer != null) {
            return;
        }
        try {
            JavaType javaType = new JavaType(Object.class);
            List<Class<?>> list = Collections.emptyList();
            jsonTransformer = (Transformer<Object, String>) registry.getTransformer(javaType, JSON_TYPE, list, list);
            if (jsonTransformer == null) {
                throw new ServiceRuntimeException("JSON transformer not found. Ensure that the JSON databinding extension is installed");
            }
        } catch (TransformationException e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
