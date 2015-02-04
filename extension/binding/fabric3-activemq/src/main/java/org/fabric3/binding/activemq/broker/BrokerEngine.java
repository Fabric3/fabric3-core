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
package org.fabric3.binding.activemq.broker;

import javax.management.MBeanServer;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.store.amq.AMQPersistenceAdapter;
import org.fabric3.api.annotation.Source;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocator;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Creates an embedded ActiveMQ broker.
 */
@EagerInit
public class BrokerEngine {
    private static final int DEFAULT_PORT = 61616;

    private PortAllocator portAllocator;
    private HostInfo info;

    private BrokerService broker;
    private File tempDir;
    private Port selectedPort;
    private int jmsPort = -1;
    private String bindAddress;
    private File dataDir;
    private BrokerConfiguration brokerConfiguration;
    private MonitorLevel monitorLevel = MonitorLevel.WARNING;
    private MBeanServer mBeanServer;
    private boolean disabled;
    private String configuredBrokerName;

    public BrokerEngine(@Reference PortAllocator portAllocator, @Reference HostInfo info) {
        this.portAllocator = portAllocator;
        tempDir = new File(info.getTempDir(), "activemq");
        // sets the directory where persistent messages are written
        File baseDataDir = info.getDataDir();
        dataDir = new File(baseDataDir, "activemq.data");
        this.info = info;
    }

    @Property(required = false)
    @Source("$systemConfig/f3:runtime/@broker.name")
    public void setConfiguredBrokerName(String configuredBrokerName) {
        this.configuredBrokerName = configuredBrokerName;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:jms/f3:activemq/@port")
    public void setJmsPort(int port) {
        this.jmsPort = port;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:jms/f3:activemq/@logging")
    public void setMonitorLevel(String monitorLevel) {
        this.monitorLevel = MonitorLevel.valueOf(monitorLevel);
    }

    @Property(required = false)
    @Source("$systemConfig/f3:runtime/@host.address")
    public void setDefaultBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    @Property(required = false)
    @Source("$systemConfig//f3:jms/f3:activemq")
    public void setBrokerConfig(XMLStreamReader reader) throws Fabric3Exception, XMLStreamException {
        BrokerParser parser = new BrokerParser();
        brokerConfiguration = parser.parse(reader);
    }

    @Property(required = false)
    @Source("$systemConfig//f3:jms/f3:activemq/@broker.disabled")
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Reference(required = false)
    public void setMBeanServer(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Init
    public void init() throws Exception {
        if (disabled) {
            return;
        }
        selectPort();
        if (bindAddress == null) {
            // if the host address is not specified, use localhost address
            bindAddress = InetAddress.getLocalHost().getHostAddress();
        }
        // set the default broker name
        String brokerName;
        if (configuredBrokerName != null) {
            brokerName = configuredBrokerName;
        } else {
            brokerName = info.getRuntimeName().replace(":", ".");
        }
        broker = new BrokerService();
        broker.setUseJmx(mBeanServer != null);
        broker.setTmpDataDirectory(tempDir);
        broker.setDataDirectory(dataDir.toString());
        if (brokerConfiguration == null) {
            // default configuration
            broker.setBrokerName(brokerName);
            createManagementContext(brokerName);
            selectedPort.bind(Port.TYPE.TCP);
            TransportConnector connector = broker.addConnector("tcp://" + bindAddress + ":" + selectedPort.getNumber());
            String group = info.getDomain().getAuthority();
            connector.setDiscoveryUri(URI.create("multicast://default?group=" + group));
            broker.addNetworkConnector("multicast://default?group=" + group);
        } else {
            String name = brokerConfiguration.getName();
            if (name != null) {
                brokerName = name;
                broker.setBrokerName(name);
            } else {
                broker.setBrokerName(brokerName);
            }
            createManagementContext(brokerName);
            PersistenceAdapterConfig persistenceConfig = brokerConfiguration.getPersistenceAdapter();
            if (persistenceConfig != null) {
                if (PersistenceAdapterConfig.Type.AMQ == persistenceConfig.getType()) {
                    AMQPersistenceAdapter adapter = new AMQPersistenceAdapter();
                    adapter.setIndexBinSize(persistenceConfig.getIndexBinSize());
                    adapter.setCheckpointInterval(persistenceConfig.getCheckpointInterval());
                    adapter.setCleanupInterval(persistenceConfig.getCleanupInterval());
                    adapter.setIndexKeySize(persistenceConfig.getIndexKeySize());
                    adapter.setIndexPageSize(persistenceConfig.getIndexPageSize());
                    adapter.setSyncOnWrite(persistenceConfig.isSyncOnWrite());
                    adapter.setDisableLocking(persistenceConfig.isDisableLocking());
                    broker.setPersistenceAdapter(adapter);
                }
            }
            for (URI uri : brokerConfiguration.getNetworkConnectorUris()) {
                broker.addNetworkConnector(uri);
            }
            for (TransportConnectorConfig config : brokerConfiguration.getTransportConnectorConfigs()) {
                URI uri = config.getUri();
                URI discoveryUri = config.getDiscoveryUri();
                TransportConnector connector = broker.addConnector(uri);
                connector.setDiscoveryUri(discoveryUri);
            }
        }
        broker.start();
    }

    private void createManagementContext(String brokerName) {
        Fabric3ManagementContext context = new Fabric3ManagementContext(brokerName, mBeanServer);
        broker.setManagementContext(context);
    }

    @Destroy
    public void destroy() throws Exception {
        if (broker != null) {
            broker.stop();
        }
    }

    private void selectPort() throws Fabric3Exception {
        if (jmsPort == -1) {
            // port not assigned, get one from the allocator
            if (portAllocator.isPoolEnabled()) {
                selectedPort = portAllocator.allocate("JMS", "JMS");
            } else {
                selectedPort = portAllocator.reserve("JMS", "JMS", DEFAULT_PORT);
            }
        } else {
            // port is explicitly assigned
            selectedPort = portAllocator.reserve("JMS", "JMS", jmsPort);
        }

    }

}
