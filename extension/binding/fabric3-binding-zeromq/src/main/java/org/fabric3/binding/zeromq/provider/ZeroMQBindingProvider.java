/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.binding.zeromq.provider;

import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Property;

import org.fabric3.binding.zeromq.common.ZeroMQMetadata;
import org.fabric3.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.spi.binding.provider.BindingMatchResult;
import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.binding.provider.BindingSelectionException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * A binding.sca implementation that uses ZeroMQ as the underlying transport.
 *
 * @version $Rev$ $Date$
 */
public class ZeroMQBindingProvider implements BindingProvider {
    private static final BindingMatchResult MATCH = new BindingMatchResult(true, ZeroMQBindingDefinition.BINDING_0MQ);
    private static final BindingMatchResult NO_MATCH = new BindingMatchResult(false, ZeroMQBindingDefinition.BINDING_0MQ);

    private boolean enabled = true;

    @Property(required = false)
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public QName getType() {
        return ZeroMQBindingDefinition.BINDING_0MQ;
    }

    public BindingMatchResult canBind(LogicalWire wire) {
        return NO_MATCH;  // not yet implemented
    }

    public BindingMatchResult canBind(LogicalChannel channel) {
        return !enabled ? NO_MATCH : MATCH;
    }

    public void bind(LogicalWire wire) throws BindingSelectionException {
        throw new UnsupportedOperationException();
    }

    public void bind(LogicalChannel channel) throws BindingSelectionException {
        ZeroMQMetadata metadata = new ZeroMQMetadata();
        metadata.setChannelName(channel.getDefinition().getName());
        ZeroMQBindingDefinition definition = new ZeroMQBindingDefinition("binding.zeromq", metadata);
        LogicalBinding<ZeroMQBindingDefinition> binding = new LogicalBinding<ZeroMQBindingDefinition>(definition, channel);
        channel.addBinding(binding);
    }
}
