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
package org.fabric3.federation.node.merge;

import java.util.Iterator;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.domain.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
public class DomainMergeServiceImpl implements DomainMergeService {
    private LogicalComponentManager lcm;

    private String zoneName;

    public DomainMergeServiceImpl(@Reference(name = "lcm") LogicalComponentManager lcm, @Reference HostInfo info) {
        this.lcm = lcm;
        this.zoneName = info.getZoneName();
    }

    public void merge(LogicalCompositeComponent snapshot) {
        LogicalCompositeComponent domain = lcm.getRootComponent();
        for (LogicalComponent<?> component : snapshot.getComponents()) {
            if (zoneName.equals(component.getZone())) {
                // do not merge components for the current zone
                continue;
            }
            if (LogicalState.NEW == component.getState()) {
                component.setState(LogicalState.PROVISIONED);
                domain.addComponent(component);
            } else if (LogicalState.MARKED == component.getState()) {
                domain.removeComponent(component.getUri());
            }
        }
        for (LogicalChannel channel : snapshot.getChannels()) {
            if (zoneName.equals(channel.getZone())) {
                // do not merge channels for the current zone
                continue;
            }
            if (LogicalState.NEW == channel.getState()) {
                channel.setState(LogicalState.PROVISIONED);
                domain.addChannel(channel);
            } else if (LogicalState.MARKED == channel.getState()) {
                domain.removeChannel(channel.getUri());
            }
        }
        // FIXME support reinjection - need to call generation and deployment
    }

    public void drop(String zone) {
        LogicalCompositeComponent domain = lcm.getRootComponent();
        for (Iterator<LogicalComponent<?>> iterator = domain.getComponents().iterator(); iterator.hasNext(); ) {
            LogicalComponent<?> component = iterator.next();
            if (zone.equals(component.getZone())) {
                iterator.remove();
            }
        }
        for (Iterator<LogicalChannel> iterator = domain.getChannels().iterator(); iterator.hasNext(); ) {
            LogicalChannel channel = iterator.next();
            if (zone.equals(channel.getZone())) {
                iterator.remove();
            }
        }
        // FIXME support reinjection - need to call generation and deployment
    }
}
