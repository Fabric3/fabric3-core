/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.activemq.broker;

import java.io.IOException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.activemq.broker.jmx.AnnotatedMBean;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.broker.jmx.SubscriptionViewMBean;

/**
 * Overrides the ActiveMQ management context to use the Fabric3 runtime MBean server and map default ActiveMQ JMX MBean names to Fabric3 conventions.
 */
public class Fabric3ManagementContext extends ManagementContext {
    private MBeanServer mBeanServer;

    public Fabric3ManagementContext(String brokerName, MBeanServer mBeanServer) {
        this.mBeanServer = new MBeanServerWrapper(brokerName, mBeanServer);
        // force MBeanServer to be set in parent
        super.getMBeanServer();
    }

    public void start() throws IOException {
        // override default behavior
    }

    public void stop() throws IOException {
        // override default behavior
    }

    public MBeanServer getMBeanServer() {
        return mBeanServer;
    }

    public boolean isCreateMBeanServer() {
        return false;
    }

    public boolean isFindTigerMbeanServer() {
        return false;
    }

    protected MBeanServer findMBeanServer() {
        return mBeanServer;
    }

    protected MBeanServer createMBeanServer() throws MalformedObjectNameException, IOException {
        return mBeanServer;
    }

    public ObjectInstance registerMBean(Object bean, ObjectName name) throws Exception {
        if (bean instanceof AnnotatedMBean) {
            Object impl = ((AnnotatedMBean) bean).getImplementation();
            if (impl instanceof SubscriptionViewMBean) {
                return null;
            }
        } else if (bean instanceof SubscriptionViewMBean) {
            return null;
        }
        return super.registerMBean(bean, name);
    }
}