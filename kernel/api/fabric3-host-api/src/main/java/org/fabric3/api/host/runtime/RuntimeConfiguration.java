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
package org.fabric3.api.host.runtime;

import javax.management.MBeanServer;

import org.fabric3.api.host.monitor.DestinationRouter;

/**
 * Contains host dependencies required to boot a runtime instance.
 */
public class RuntimeConfiguration {
    private HostInfo hostInfo;
    private MBeanServer mBeanServer;
    private DestinationRouter router;

    /**
     * Constructor taking the minimal host dependencies to boot a runtime.
     *
     * @param hostInfo    the host info instance
     * @param mBeanServer the JMX MBean server
     * @param router      the destination router
     */
    public RuntimeConfiguration(HostInfo hostInfo, MBeanServer mBeanServer, DestinationRouter router) {
        this.hostInfo = hostInfo;
        this.mBeanServer = mBeanServer;
        this.router = router;
    }
    /**
     * Returns the runtime host info.
     *
     * @return the runtime host info
     */
    public HostInfo getHostInfo() {
        return hostInfo;
    }

    /**
     * Returns the MBeanServer.
     *
     * @return the MBeanServer
     */
    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    /**
     * Returns the destination router
     *
     * @return the router
     */
    public DestinationRouter getDestinationRouter() {
        return router;
    }

}
