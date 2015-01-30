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
package org.fabric3.management.rest.framework.runtime;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.management.rest.framework.AbstractResourceService;
import org.fabric3.management.rest.model.Resource;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Listens for managed artifacts exported under the /runtime path and registers them as sub-resources of the runtime resource.
 */
@EagerInit
@Management(path = "/runtime")
public class RuntimeResourceService extends AbstractResourceService {
    private static final String RUNTIME_PATH = "/runtime";
    private HostInfo info;

    public RuntimeResourceService(@Reference HostInfo info) {
        this.info = info;
    }

    @Override
    protected String getResourcePath() {
        return RUNTIME_PATH;
    }

    @Override
    protected void populateResource(Resource resource, HttpServletRequest request) {
        resource.setProperty("name", getName());
        resource.setProperty("domain", getDomain());
        resource.setProperty("mode", getMode());
    }

    @ManagementOperation(description = "The runtime name")
    public String getName() {
        return info.getRuntimeName();
    }

    @ManagementOperation(description = "The domain URI this runtime is a part of ")
    public URI getDomain() {
        return info.getDomain();
    }

    @ManagementOperation(description = "The runtime mode")
    public RuntimeMode getMode() {
        return info.getRuntimeMode();
    }

}
