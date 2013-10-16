package org.fabric3.binding.activemq.provider;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.host.runtime.HostInfo;

/**
 *
 */
public class BrokerHelperImpl implements BrokerHelper {
    private String defaultBrokerName;

    public BrokerHelperImpl(@Reference HostInfo info) {
        this.defaultBrokerName = "vm://" + info.getRuntimeName().replace(":", ".");
    }

    public String getDefaultBrokerName() {
        return defaultBrokerName;
    }
}
