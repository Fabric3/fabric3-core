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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.runtime.weblogic.federation;

import javax.management.JMException;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.domain.allocator.AllocationException;
import org.fabric3.spi.domain.allocator.Allocator;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.plan.DeploymentPlan;

/**
 * Allocator that sets the zone to the current domain name.
 */
@EagerInit
public class WebLogicAllocator implements Allocator {
    private JmxHelper jmxHelper;
    private String domainName;

    public WebLogicAllocator(@Reference JmxHelper jmxHelper) {
        this.jmxHelper = jmxHelper;
    }

    @Init
    public void init() throws JMException {
        domainName = jmxHelper.getRuntimeJmxAttribute(String.class, "DomainConfiguration/Name");
    }

    public void allocate(LogicalComponent<?> component, DeploymentPlan plan) {
        component.setZone(domainName);
    }

    public void allocate(LogicalChannel channel, DeploymentPlan plan) {
        channel.setZone(domainName);
    }

    public void allocate(LogicalResource<?> resource, DeploymentPlan plan) throws AllocationException {
        resource.setZone(domainName);
    }
}