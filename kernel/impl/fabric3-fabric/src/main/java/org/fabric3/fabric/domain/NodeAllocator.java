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
package org.fabric3.fabric.domain;

import org.fabric3.api.host.Names;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.domain.allocator.AllocationException;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Handles allocation on node runtimes.
 */
@EagerInit
public class NodeAllocator implements Allocator {
    private String zoneName;
    private boolean enabled;

    public NodeAllocator(@Reference HostInfo info) {
        zoneName = info.getZoneName();
        enabled = RuntimeMode.NODE == info.getRuntimeMode();
    }

    public void allocate(LogicalComponent<?> component) throws AllocationException {
        if (!enabled) {
            return;
        }
        if (Names.LOCAL_ZONE.equals(component.getZone())) {
            if (component instanceof LogicalCompositeComponent) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
                for (LogicalComponent<?> child : composite.getComponents()) {
                    allocate(child);
                }
                for (LogicalResource<?> resource : composite.getResources()) {
                    allocate(resource);
                }
                for (LogicalChannel channel : composite.getChannels()) {
                    allocate(channel);
                }
            }
            component.setZone(zoneName);
        }
    }

    public void allocate(LogicalChannel channel) throws AllocationException {
        if (!enabled) {
            return;
        }
        channel.setZone(zoneName);
    }

    public void allocate(LogicalResource<?> resource) throws AllocationException {
        if (!enabled) {
            return;
        }
        resource.setZone(zoneName);
    }

}
