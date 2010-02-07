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

import java.net.URI;
import javax.management.MBeanServer;

import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.repository.Repository;

/**
 * Represents a runtime in a domain.
 *
 * @version $Rev$ $Date$
 */
public interface Fabric3Runtime<HI extends HostInfo> {

    /**
     * Sets the dependencies required from the host environment.
     *
     * @param configuration the dependencies required from the host environment
     */
    void setConfiguration(RuntimeConfiguration<HI> configuration);

    /**
     * Returns the host ClassLoader that is parent to all Fabric3 classloaders.
     *
     * @return the host's ClassLoader
     */
    ClassLoader getHostClassLoader();

    /**
     * Returns the type of info supplied by the host.
     *
     * @return the type of info supplied by the host
     */
    Class<HI> getHostInfoType();

    /**
     * Returns the info this runtime will make available to service components.
     *
     * @return the info this runtime will make available to service components
     */
    HI getHostInfo();

    /**
     * Returns the MonitorFactory that this runtime is using.
     *
     * @return the MonitorFactory that this runtime is using
     */
    MonitorFactory getMonitorFactory();

    /**
     * Returns the MBeanServer this runtime should use.
     *
     * @return the MBeanServer
     */
    MBeanServer getMBeanServer();

    /**
     * Returns the runtime repository.
     *
     * @return the runtime repository.
     */
    Repository getRepository();

    /**
     * Returns the system component providing the designated service.
     *
     * @param service the service interface required
     * @param uri     the id of the system component
     * @param <I>     the Java type for the service interface
     * @return an implementation of the requested service
     */
    <I> I getComponent(Class<I> service, URI uri);

    /**
     * Boots core services in the runtime.
     *
     * @throws InitializationException if there is an error initializing the runtime
     */
    void boot() throws InitializationException;

    /**
     * Destroy the runtime. Any further invocations should result in an error.
     *
     * @throws ShutdownException if there is an error destroying the runtime
     */
    void destroy() throws ShutdownException;

}
