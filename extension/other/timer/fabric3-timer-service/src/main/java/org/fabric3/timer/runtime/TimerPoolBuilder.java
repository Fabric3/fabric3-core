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
package org.fabric3.timer.runtime;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.builder.resource.ResourceBuilder;
import org.fabric3.timer.provision.PhysicalTimerPoolResource;
import org.fabric3.timer.spi.TimerService;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class TimerPoolBuilder implements ResourceBuilder<PhysicalTimerPoolResource> {
    private TimerService service;

    public TimerPoolBuilder(@Reference TimerService service) {
        this.service = service;
    }

    public void build(PhysicalTimerPoolResource definition) throws ContainerException {
        service.allocate(definition.getName(), definition.getCoreSize());
    }

    public void remove(PhysicalTimerPoolResource definition) throws ContainerException {
        service.deallocate(definition.getName());
    }
}