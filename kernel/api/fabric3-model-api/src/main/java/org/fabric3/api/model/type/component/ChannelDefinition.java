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
package org.fabric3.api.model.type.component;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A channel configuration in a composite.
 */
public class ChannelDefinition extends BindableDefinition<Composite> {
    public static final String DEFAULT_TYPE = "default";

    private static final long serialVersionUID = 8735705202863105855L;

    private String name;
    private String type = DEFAULT_TYPE;
    private boolean local;

    private Map<QName, Serializable> metadata = new HashMap<>();

    public ChannelDefinition(String name) {
        this.name = name;
    }

    public ChannelDefinition(String name, String type, boolean local) {
        this.name = name;
        this.type = type;
        this.local = local;
    }

    /**
     * Returns the channel name.
     *
     * @return the channel name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the channel type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the channel type.
     *
     * @param type the channel type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * True if the channel is local.
     *
     * @return true if the channel is local
     */
    public boolean isLocal() {
        return local;
    }

    /**
     * Sets if the channel is local.
     *
     * @param local true if the channel is local
     */
    public void setLocal(boolean local) {
        this.local = local;
    }

    public void addMetadata(QName name, Serializable data) {
        metadata.put(name, data);
    }

    public <T> T getMetadata(QName name, Class<T> type) {
        return type.cast(metadata.get(name));
    }

    public Map<QName, Serializable> getMetadata() {
        return metadata;
    }
}
