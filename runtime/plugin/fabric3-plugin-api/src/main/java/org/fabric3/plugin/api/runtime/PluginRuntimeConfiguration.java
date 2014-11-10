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
package org.fabric3.plugin.api.runtime;

import javax.management.MBeanServer;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.fabric3.api.host.monitor.DestinationRouter;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.RuntimeConfiguration;

/**
 * Configuration for a plugin runtime.
 */
public class PluginRuntimeConfiguration extends RuntimeConfiguration {
    private RepositorySystem system;
    private RepositorySystemSession session;

    public PluginRuntimeConfiguration(HostInfo hostInfo,
                                      MBeanServer mBeanServer,
                                      DestinationRouter router,
                                      RepositorySystem system,
                                      RepositorySystemSession session) {
        super(hostInfo, mBeanServer, router);
        this.system = system;
        this.session = session;
    }

    public RepositorySystem getSystem() {
        return system;
    }

    public RepositorySystemSession getSession() {
        return session;
    }
}
