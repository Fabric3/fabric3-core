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
    private boolean replicate;
    private PhysicalChannelBindingDefinition bindingDefinition;
    private String type;
    private ChannelDeliveryType deliveryType;
    private ChannelSide channelSide = ChannelSide.PRODUCER;

    private Serializable metadata;

    public PhysicalChannelDefinition(URI uri, QName deployable, boolean replicate) {
        this(uri, deployable, replicate, ChannelConstants.DEFAULT_TYPE, ChannelDeliveryType.DEFAULT);
    }

    public PhysicalChannelDefinition(URI uri, QName deployable, boolean replicate, String type, ChannelDeliveryType deliveryType) {
        this.uri = uri;
        this.deployable = deployable;
        this.replicate = replicate;
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
     * Returns true if the channel replicates events to all channel instances in a zone.
     *
     * @return true if the channel replicates events to all channel instances in a zone
     */
    public boolean isReplicate() {
        return replicate;
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