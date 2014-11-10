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
package org.fabric3.binding.activemq.broker;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.io.IOException;

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