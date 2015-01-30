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
package org.fabric3.spi.model.instance;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.host.Names;
import org.fabric3.api.model.type.component.ChannelDefinition;

/**
 * An instantiated channel in the domain.
 */
public class LogicalChannel extends LogicalBindable {
    private static final long serialVersionUID = -1098943196013754799L;

    private ChannelDefinition definition;

    private String zone = Names.LOCAL_ZONE;
    private QName deployable;
    private LogicalState state = LogicalState.NEW;

    public LogicalChannel(URI uri, ChannelDefinition definition, LogicalCompositeComponent parent) {
        super(uri, null, parent);
        this.definition = definition;
    }

    /**
     * Returns the ChannelDefinition for this channel.
     *
     * @return the ChannelDefinition for this channel
     */
    public ChannelDefinition getDefinition() {
        return definition;
    }

    /**
     * Returns the zone name where the channel is provisioned.
     *
     * @return the zone name where the channel is provisioned
     */
    public String getZone() {
        return zone;
    }

    /**
     * Sets the zone name where the channel is provisioned.
     *
     * @param zone the zone name where the channel is provisioned
     */
    public void setZone(String zone) {
        this.zone = zone;
    }

    /**
     * Returns the deployable composite name this logical channel was instantiated from.
     *
     * @return the deployable name
     */
    public QName getDeployable() {
        return deployable;
    }

    /**
     * Sets the name of the deployable composite this channel was instantiated from.
     *
     * @param deployable the deployable name
     */
    public void setDeployable(QName deployable) {
        this.deployable = deployable;
    }

    /**
     * Returns the instance state.
     *
     * @return the instance state
     */
    public LogicalState getState() {
        return state;
    }

    /**
     * Sets the instance state.
     *
     * @param state the instance state
     */
    public void setState(LogicalState state) {
        this.state = state;
    }

    /**
     * Convenience getter as channels can be configured with only one binding.
     *
     * @return the binding or null if the channel is not bound
     */
    public LogicalBinding getBinding() {
        if (getBindings().isEmpty()) {
            return null;
        }
        return getBindings().get(0);
    }

    @Override
    public void addBinding(LogicalBinding<?> binding) {
        if (!getBindings().isEmpty()) {
            throw new IllegalStateException("Channel is already configured with a binding");
        }
        super.addBinding(binding);
    }

}