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
package org.fabric3.binding.activemq.broker;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import javax.management.MBeanServer;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.store.amq.AMQPersistenceAdapter;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.binding.activemq.factory.InvalidConfigurationException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.monitor.MonitorService;

/**
 * Creates an embedded ActiveMQ broker.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class BrokerEngine {
    private String brokerName;
    private BrokerService broker;
    private File tempDir;
    private int selectedPort = 61616;
    private String bindAddress;
    private int maxPort = 71717;
    private int minPort = 61616;
    private File dataDir;
    private BrokerConfiguration brokerConfiguration;
    private MonitorLevel monitorLevel = MonitorLevel.WARNING;
    private MonitorService monitorService;
    private MBeanServer mBeanServer;

    public BrokerEngine(@Reference HostInfo info) {
        tempDir = new File(info.getTempDir(), "activemq");
        // sets the directory where persistent messages are written
        File baseDataDir = info.getDataDir();
        dataDir = new File(baseDataDir, "activemq.data");
        // set the broker name to the runtime id
        brokerName = info.getRuntimeId();
    }

    @Property(required = false)
    public void setMinPort(int minPort) {
        this.minPort = minPort;
    }

    @Property(required = false)
    public void setMaxPort(int maxPort) {
        this.maxPort = maxPort;
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
    public void setBrokerConfig(XMLStreamReader reader) throws InvalidConfigurationException, XMLStreamException {
        BrokerParser parser = new BrokerParser();
        brokerConfiguration = parser.parse(reader);
    }

    @Reference(required = false)
    public void setMBeanServer(MBeanServer mBeanServer) {
        this.mBeanServer = mBeanServer;
    }

    @Init
    public void init() throws Exception {
        if (bindAddress == null) {
            // if the host address is not specified, use localhost address
            bindAddress = InetAddress.getLocalHost().getHostAddress();
        }
        // ActiveMQ default level is INFO which is verbose. Only log warnings by default
//        monitorService.setProviderLevel("org.apache.activemq", monitorLevel.toString());
        broker = new BrokerService();
        Fabric3ManagementContext context = new Fabric3ManagementContext(brokerName, mBeanServer);
        broker.setManagementContext(context);
        broker.setUseJmx(true);
        broker.setTmpDataDirectory(tempDir);
        broker.setDataDirectory(dataDir.toString());
        if (brokerConfiguration == null) {
            // default configuration
            broker.setBrokerName(brokerName);
            boolean loop = true;
            TransportConnector connector = null;
            while (loop) {
                try {
                    connector = broker.addConnector("tcp://" + bindAddress + ":" + selectedPort);
                    loop = false;
                } catch (IOException e) {
                    selectPort();
                }
            }
            connector.setDiscoveryUri(URI.create("multicast://default"));
            broker.addNetworkConnector("multicast://default");
        } else {
            String name = brokerConfiguration.getName();
            if (name != null) {
                broker.setBrokerName(name);
            } else {
                broker.setBrokerName(brokerName);
            }
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
        }
        broker.start();
    }

    @Destroy
    public void destroy() throws Exception {
        if (broker != null) {
            broker.stop();
        }
    }

    private void selectPort() throws IOException {
        if (maxPort == -1) {
            selectedPort = minPort;
            return;
        }
        selectedPort = minPort;
        while (selectedPort <= maxPort) {
            try {
                ServerSocket socket = new ServerSocket(selectedPort);
                socket.close();
                return;
            } catch (IOException e) {
                selectedPort++;
            }
        }
        selectedPort = -1;
        throw new IOException(
                "Unable to find an available port. Check to ensure the system configuration specifies an open port or port range.");
    }


}
