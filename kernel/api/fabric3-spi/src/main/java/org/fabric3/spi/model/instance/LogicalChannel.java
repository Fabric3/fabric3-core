/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.model.instance;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.model.type.component.ChannelDefinition;

/**
 * An instantiated channel in the domain.
 *
 * @version $Rev$ $Date$
 */
public class LogicalChannel extends Bindable {
    private static final long serialVersionUID = -1098943196013754799L;

    public static final String LOCAL_ZONE = "LocalZone";

    private ChannelDefinition definition;

    private String zone = LOCAL_ZONE;
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

    /**
     * Convenience getter as channels can be configured with only one binding.
     */
    public void clearBinding() {
        getBindings().clear();
    }

    /**
     * Convenience getter as channels can be configured with only one binding.
     *
     * @return true if the channel is configured with a binding
     */
    public boolean isBound() {
        return !getBindings().isEmpty();
    }

    @Override
    public void addBinding(LogicalBinding<?> binding) {
        if (!getBindings().isEmpty()) {
            throw new IllegalStateException("Channel is already configured with a binding");
        }
        super.addBinding(binding);
    }

}