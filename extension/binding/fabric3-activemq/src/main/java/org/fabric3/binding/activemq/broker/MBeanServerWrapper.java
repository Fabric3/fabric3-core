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

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.StandardMBean;
import javax.management.loading.ClassLoaderRepository;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.activemq.broker.jmx.BrokerView;
import org.apache.activemq.broker.jmx.ConnectionView;
import org.apache.activemq.broker.jmx.ConnectorView;
import org.apache.activemq.broker.jmx.JmsConnectorView;
import org.apache.activemq.broker.jmx.NetworkBridgeView;
import org.apache.activemq.broker.jmx.NetworkConnectorView;
import org.apache.activemq.broker.jmx.ProxyConnectorView;
import org.apache.activemq.broker.jmx.QueueView;
import org.apache.activemq.broker.jmx.SubscriptionView;
import org.apache.activemq.broker.jmx.TopicView;
import org.apache.activemq.util.JMXSupport;

/**
 * Maps from ActiveMQ to Fabric3 JMX MBean naming conventions.
 */
public class MBeanServerWrapper implements MBeanServer {
    private static final String DOMAIN = "fabric3";
    private String brokerName;
    private MBeanServer delegate;
    Map<ObjectName, ObjectName> mappings = new ConcurrentHashMap<>();

    public MBeanServerWrapper(String brokerName, MBeanServer delegate) {
        this.brokerName = brokerName.replace(":", ".");
        this.delegate = delegate;
    }

    public ObjectInstance createMBean(String className, ObjectName name)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException {
        return delegate.createMBean(className, name);
    }

    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException {
        return delegate.createMBean(className, name, loaderName);
    }

    public ObjectInstance createMBean(String className, ObjectName name, Object[] params, String[] signature)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException {
        return delegate.createMBean(className, name, params, signature);
    }

    public ObjectInstance createMBean(String className, ObjectName name, ObjectName loaderName, Object[] params, String[] signature)
            throws ReflectionException, InstanceAlreadyExistsException, MBeanException, NotCompliantMBeanException,
            InstanceNotFoundException {
        return delegate.createMBean(className, name, loaderName, params, signature);
    }

    public ObjectInstance registerMBean(Object object, ObjectName original)
            throws InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        ObjectName name = convertName(original, object);
        if (delegate.isRegistered(name)) {
            try {
                delegate.unregisterMBean(name);
            } catch (InstanceNotFoundException e) {
                throw new MBeanRegistrationException(e);
            }
        }
        mappings.put(original, name);
        return delegate.registerMBean(object, name);
    }

    public void unregisterMBean(ObjectName name) throws InstanceNotFoundException, MBeanRegistrationException {
        ObjectName mapped = mappings.remove(name);
        if (mapped == null) {
            throw new InstanceNotFoundException(name.toString());
        }
        delegate.unregisterMBean(mapped);
    }

    public ObjectInstance getObjectInstance(ObjectName name) throws InstanceNotFoundException {
        return delegate.getObjectInstance(name);
    }

    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
        return delegate.queryMBeans(name, query);
    }

    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
        return delegate.queryNames(name, query);
    }

    public boolean isRegistered(ObjectName original) {
        ObjectName name = mappings.get(original);
        return name != null && delegate.isRegistered(name);
    }

    public Integer getMBeanCount() {
        return delegate.getMBeanCount();
    }

    public Object getAttribute(ObjectName name, String attribute)
            throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException {
        return delegate.getAttribute(name, attribute);
    }

    public AttributeList getAttributes(ObjectName name, String[] attributes) throws InstanceNotFoundException, ReflectionException {
        return delegate.getAttributes(name, attributes);
    }

    public void setAttribute(ObjectName name, Attribute attribute)
            throws InstanceNotFoundException, AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        delegate.setAttribute(name, attribute);
    }

    public AttributeList setAttributes(ObjectName name, AttributeList attributes) throws InstanceNotFoundException, ReflectionException {
        return delegate.setAttributes(name, attributes);
    }

    public Object invoke(ObjectName name, String operationName, Object[] params, String[] signature)
            throws InstanceNotFoundException, MBeanException, ReflectionException {
        return delegate.invoke(name, operationName, params, signature);
    }

    public String getDefaultDomain() {
        return delegate.getDefaultDomain();
    }

    public String[] getDomains() {
        return delegate.getDomains();
    }

    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException {
        delegate.addNotificationListener(name, listener, filter, handback);
    }

    public void addNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException {
        delegate.addNotificationListener(name, listener, filter, handback);
    }

    public void removeNotificationListener(ObjectName name, ObjectName listener) throws InstanceNotFoundException, ListenerNotFoundException {
        delegate.removeNotificationListener(name, listener);
    }

    public void removeNotificationListener(ObjectName name, ObjectName listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException {
        delegate.removeNotificationListener(name, listener, filter, handback);
    }

    public void removeNotificationListener(ObjectName name, NotificationListener listener)
            throws InstanceNotFoundException, ListenerNotFoundException {
        delegate.removeNotificationListener(name, listener);
    }

    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback)
            throws InstanceNotFoundException, ListenerNotFoundException {
        delegate.removeNotificationListener(name, listener, filter, handback);
    }

    public MBeanInfo getMBeanInfo(ObjectName name) throws InstanceNotFoundException, IntrospectionException, ReflectionException {
        return delegate.getMBeanInfo(name);
    }

    public boolean isInstanceOf(ObjectName name, String className) throws InstanceNotFoundException {
        return delegate.isInstanceOf(name, className);
    }

    public Object instantiate(String className) throws ReflectionException, MBeanException {
        return delegate.instantiate(className);
    }

    public Object instantiate(String className, ObjectName loaderName) throws ReflectionException, MBeanException, InstanceNotFoundException {
        return delegate.instantiate(className, loaderName);
    }

    public Object instantiate(String className, Object[] params, String[] signature) throws ReflectionException, MBeanException {
        return delegate.instantiate(className, params, signature);
    }

    public Object instantiate(String className, ObjectName loaderName, Object[] params, String[] signature)
            throws ReflectionException, MBeanException, InstanceNotFoundException {
        return delegate.instantiate(className, loaderName, params, signature);
    }

    public ObjectInputStream deserialize(ObjectName name, byte[] data) throws OperationsException {
        return delegate.deserialize(name, data);
    }

    public ObjectInputStream deserialize(String className, byte[] data) throws OperationsException, ReflectionException {
        return delegate.deserialize(className, data);
    }

    public ObjectInputStream deserialize(String className, ObjectName loaderName, byte[] data) throws OperationsException, ReflectionException {
        return delegate.deserialize(className, loaderName, data);
    }

    public ClassLoader getClassLoaderFor(ObjectName mbeanName) throws InstanceNotFoundException {
        return delegate.getClassLoaderFor(mbeanName);
    }

    public ClassLoader getClassLoader(ObjectName loaderName) throws InstanceNotFoundException {
        return delegate.getClassLoader(loaderName);
    }

    public ClassLoaderRepository getClassLoaderRepository() {
        return delegate.getClassLoaderRepository();
    }

    private ObjectName convertName(ObjectName name, Object object) throws MBeanRegistrationException {
        if (!(object instanceof StandardMBean)){
           return name;
        }
        Object implementation = ((StandardMBean)object).getImplementation();
        if (implementation instanceof BrokerView) {
            try {
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName + ", subgroup=Broker");
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        } else if (implementation instanceof ConnectionView) {
            try {
                String connectionName = name.getKeyProperty("Connection");
                String connectorName = name.getKeyProperty("ConnectorName");
                if (connectionName != null) {
                    name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName
                            + ", subgroup=connections, connection=" + connectionName + ", ConnectorName=" + connectorName);
                } else {
                    String propertyName = name.getKeyProperty("Name");
                    name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName
                            + ", subgroup=connections, Name=" + propertyName + ", ConnectorName=" + connectorName);

                }
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        } else if (implementation instanceof ConnectorView) {
            try {
                String connectorName = name.getKeyProperty("ConnectorName");
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName
                        + ", subgroup=connectors, connectorName=" + connectorName);
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        } else if (implementation instanceof SubscriptionView) {
            SubscriptionView view = (SubscriptionView) implementation;
            String destinationType;
            if (view.isDestinationQueue()) {
                destinationType = "queues";
            } else {
                destinationType = "topics";
            }
            try {
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName
                        + ", subgroup=subscriptions, desintantionType=" + destinationType
                        + ", destinationName=" + JMXSupport.encodeObjectNamePart(view.getDestinationName())
                        + ", cliendId=" + JMXSupport.encodeObjectNamePart(view.getClientId())
                        + ", consumerId=" + view.getSubcriptionId());
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        } else if (implementation instanceof JmsConnectorView) {
            String connectorName = name.getKeyProperty("JmsConnectorName");
            try {
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName
                        + ", subgroup=JMS connectors, jmsConnectorName=" + connectorName);
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        } else if (implementation instanceof NetworkBridgeView) {
            String bridgeName = name.getKeyProperty("Name");
            try {
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName
                        + ", subgroup=network bridges, bridgeName=" + bridgeName);
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }

        } else if (implementation instanceof NetworkConnectorView) {
            NetworkConnectorView view = (NetworkConnectorView) implementation;
            try {
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName
                        + ", subgroup=network connectors, connectorName=" + JMXSupport.encodeObjectNamePart(view.getName()));
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        } else if (implementation instanceof ProxyConnectorView) {
            String connectorName = name.getKeyProperty("ProxyConnectorName");
            try {
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName=" + brokerName
                        + ", subgroup=proxy connectors, proxyConnectorName=" + connectorName);
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        } else if (implementation instanceof QueueView) {
            QueueView view = (QueueView) implementation;
            try {
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName="
                        + brokerName + ", subgroup=queues, queueName=" + JMXSupport.encodeObjectNamePart(view.getName()));
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        } else if (implementation instanceof TopicView) {
            TopicView view = (TopicView) implementation;
            try {
                name = new ObjectName(DOMAIN + ":SubDomain=runtime, type=resource, group=ActiveMQ, brokerName="
                        + brokerName + ", subgroup=topics, queueName=" + JMXSupport.encodeObjectNamePart(view.getName()));
            } catch (MalformedObjectNameException e) {
                throw new MBeanRegistrationException(e);
            }
        }
        return name;
    }

}