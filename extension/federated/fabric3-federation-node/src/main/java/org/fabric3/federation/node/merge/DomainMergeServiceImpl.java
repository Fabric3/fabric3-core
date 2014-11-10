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
