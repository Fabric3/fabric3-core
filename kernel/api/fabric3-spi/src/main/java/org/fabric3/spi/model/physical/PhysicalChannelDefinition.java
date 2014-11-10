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
package org.fabric3.spi.model.physical;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.net.URI;

/**
 * Configuration to provision a channel on a runtime.
 */
public class PhysicalChannelDefinition implements Serializable {
    private static final long serialVersionUID = 8681183877136491160L;
    private URI uri;
    private QName deployable;
    private PhysicalChannelBindingDefinition bindingDefinition;
    private String type;
    private ChannelDeliveryType deliveryType;
    private ChannelSide channelSide = ChannelSide.PRODUCER;

    private Serializable metadata;

    public PhysicalChannelDefinition(URI uri, QName deployable) {
        this(uri, deployable, ChannelConstants.DEFAULT_TYPE, ChannelDeliveryType.DEFAULT);
    }

    public PhysicalChannelDefinition(URI uri, QName deployable, String type, ChannelDeliveryType deliveryType) {
        this.uri = uri;
        this.deployable = deployable;
        this.type = type;
        this.deliveryType = deliveryType;
    }

    /**
     * Returns the channel URI.
     *
     * @return the channel URI
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Returns the deployable composite this channel is defined in.
     *
     * @return the composite qualified name
     */
    public QName getDeployable() {
        return deployable;
    }

    /**
     * Returns the binding definition for the channel.
     *
     * @return the binding definition for the channel
     */
    public PhysicalChannelBindingDefinition getBindingDefinition() {
        return bindingDefinition;
    }

    /**
     * Sets the channel binding definition.
     *
     * @param bindingDefinition the binding definition
     */
    public void setBindingDefinition(PhysicalChannelBindingDefinition bindingDefinition) {
        this.bindingDefinition = bindingDefinition;
    }

    /**
     * Returns the type of channel
     *
     * @return the type of chanel
     */

    public String getType() {
        return type;
    }

    /**
     * Returns the channel delivery type
     *
     * @return the channel delivery type
     */
    public ChannelDeliveryType getDeliveryType() {
        return deliveryType;
    }

    /**
     * Sets the channel metadata.
     *
     * @param metadata the channel metadata
     */
    public void setMetadata(Serializable metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns the channel metadata.
     *
     * @param type the channel metadata type
     * @return the channel metadata or null if not found
     */
    public <T> T getMetadata(Class<T> type) {
        return type.cast(metadata);
    }

    /**
     * Returns the side of a logical channel the physical channel implements.
     *
     * @return the side of a logical channel the physical channel implements
     */
    public ChannelSide getChannelSide() {
        return channelSide;
    }

    /**
     * Sets the side of a logical channel the physical channel implements.
     *
     * @param channelSide the side of a logical channel the physical channel implements
     */
    public void setChannelSide(ChannelSide channelSide) {
        this.channelSide = channelSide;
    }

}