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

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates configuration information for an ActiveMQ broker.
 */
public class BrokerConfiguration {
    private String name;
    private List<URI> networkConnectorUris = Collections.emptyList();
    private List<TransportConnectorConfig> transportConnectorConfigs = Collections.emptyList();
    private PersistenceAdapterConfig persistenceAdapter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<URI> getNetworkConnectorUris() {
        return networkConnectorUris;
    }

    public void setNetworkConnectorUris(List<URI> networkConnectorUris) {
        this.networkConnectorUris = networkConnectorUris;
    }

    public List<TransportConnectorConfig> getTransportConnectorConfigs() {
        return transportConnectorConfigs;
    }

    public void setTransportConnectorConfigs(List<TransportConnectorConfig> transportConnectorConfigs) {
        this.transportConnectorConfigs = transportConnectorConfigs;
    }

    public PersistenceAdapterConfig getPersistenceAdapter() {
        return persistenceAdapter;
    }

    public void setPersistenceAdapter(PersistenceAdapterConfig adaptor) {
        this.persistenceAdapter = adaptor;
    }

}
