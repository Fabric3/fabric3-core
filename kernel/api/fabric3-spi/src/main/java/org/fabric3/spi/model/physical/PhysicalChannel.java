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

import java.net.URI;

/**
 * Metadata to provision a channel.
 */
public class PhysicalChannel {
    private URI uri;
    private boolean bound;
    private String type;
    private DeliveryType deliveryType;
    private ChannelSide channelSide = ChannelSide.PRODUCER;

    private Object metadata;
    private URI contributionUri;

    public PhysicalChannel(URI uri, URI contributionUri) {
        this(uri, ChannelConstants.DEFAULT_TYPE, DeliveryType.DEFAULT, contributionUri);
    }

    public PhysicalChannel(URI uri, String type, DeliveryType deliveryType, URI contributionUri) {
        this.uri = uri;
        this.contributionUri = contributionUri;
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
     * Returns the contribution Uri this channel is deployed for.
     *
     * @return the composite qualified name
     */
    public URI getContributionUri() {
        return contributionUri;
    }

    /**
     * Returns if the channel is bound.
     *
     * @return true if the channel is bound
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * Sets if the channel is bound.
     *
     * @param value if the channel is bound
     */
    public void setBound(boolean value) {
        this.bound = value;
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
    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    /**
     * Sets the channel metadata.
     *
     * @param metadata the channel metadata
     */
    public void setMetadata(Object metadata) {
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