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
*/
package org.fabric3.host.runtime;

import javax.management.MBeanServer;

import org.fabric3.host.monitor.MonitorEventDispatcher;
import org.fabric3.host.repository.Repository;

/**
 * Contains host dependencies required to boot a runtime instance.
 *
 * @version $Rev$ $Date$
 */
public class RuntimeConfiguration {
    private HostInfo hostInfo;
    private MBeanServer mBeanServer;
    private MonitorEventDispatcher runtimeDispatcher;
    private MonitorEventDispatcher appDispatcher;
    private Repository repository;

    /**
     * Constructor taking the minimal host dependencies to boot a runtime.
     *
     * @param hostInfo          the host info instance
     * @param mBeanServer       the JMX MBean server
     * @param runtimeDispatcher the dispatcher for handling runtime events
     * @param appDispatcher     the dispatcher for handling application events
     */
    public RuntimeConfiguration(HostInfo hostInfo,
                                MBeanServer mBeanServer,
                                MonitorEventDispatcher runtimeDispatcher,
                                MonitorEventDispatcher appDispatcher) {
        this.hostInfo = hostInfo;
        this.mBeanServer = mBeanServer;
        this.runtimeDispatcher = runtimeDispatcher;
        this.appDispatcher = appDispatcher;
    }

    /**
     * Constructor taking all configurable dependencies to boot a runtime.
     *
     * @param hostInfo          the host info instance
     * @param mBeanServer       the JMX MBean server
     * @param runtimeDispatcher the dispatcher for handling runtime events
     * @param appDispatcher     the dispatcher for handling application events
     * @param repository        the artifact repository
     */
    public RuntimeConfiguration(HostInfo hostInfo,
                                MBeanServer mBeanServer,
                                MonitorEventDispatcher runtimeDispatcher,
                                MonitorEventDispatcher appDispatcher,
                                Repository repository) {
        this.hostInfo = hostInfo;
        this.mBeanServer = mBeanServer;
        this.runtimeDispatcher = runtimeDispatcher;
        this.appDispatcher = appDispatcher;
        this.repository = repository;
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
     * Returns the dispatcher for handling runtime monitor events
     *
     * @return the dispatcher for handling runtime monitor events
     */
    public MonitorEventDispatcher getRuntimeDispatcher() {
        return runtimeDispatcher;
    }

    /**
     * Returns the dispatcher for handling application monitor events
     *
     * @return the dispatcher for handling application monitor events
     */
    public MonitorEventDispatcher getAppDispatcher() {
        return appDispatcher;
    }

    /**
     * Returns the runtime repository
     *
     * @return the runtime repository
     */
    public Repository getRepository() {
        return repository;
    }

}
