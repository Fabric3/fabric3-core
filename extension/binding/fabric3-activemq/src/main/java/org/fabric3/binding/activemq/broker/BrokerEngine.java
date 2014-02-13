/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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

import javax.management.MBeanServer;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.store.amq.AMQPersistenceAdapter;
import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.host.Port;
import org.fabric3.spi.host.PortAllocationException;
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

    public BrokerEngine(@Reference PortAllocator portAllocator, @Reference HostInfo info) {
        this.portAllocator = portAllocator;
        tempDir = new File(info.getTempDir(), "activemq");
        // sets the directory where persistent messages are written
        File baseDataDir = info.getDataDir();
        dataDir = new File(baseDataDir, "activemq.data");
        this.info = info;
    }

    @Property(required = false)
    public void setMinPort(int minPort) {
        throw new IllegalArgumentException("Port ranges no longer supported via JMS configuration. Use the runtime port.range attribute");
    }

    @Property(required = false)
    public void setMaxPort(int maxPort) {
        throw new IllegalArgumentException("Port ranges no longer supported via JMS configuration. Use the runtime port.range attribute");
    }

    @Property(required = false)
    public void setJmsPort(int port) {
        this.jmsPort = port;
    }

    @Property(required = false)
    public void setMonitorLevel(String monitorLevel) {
        this.monitorLevel = MonitorLevel.valueOf(monitorLevel);
    }

    @Property(required = false)
    public void setDefaultBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    @Property(required = false)
    public void setBrokerConfig(XMLStreamReader reader) throws InvalidBrokerConfigurationException, XMLStreamException {
        BrokerParser parser = new BrokerParser();
        brokerConfiguration = parser.parse(reader);
    }

    @Property(required = false)
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
        String brokerName = info.getRuntimeName().replace(":", ".");
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

    private void selectPort() throws PortAllocationException {
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
