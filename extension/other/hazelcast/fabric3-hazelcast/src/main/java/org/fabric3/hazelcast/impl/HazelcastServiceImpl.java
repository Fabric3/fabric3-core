package org.fabric3.hazelcast.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.hazelcast.config.Config;
import com.hazelcast.config.MemberAttributeConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.fabric3.api.MonitorChannel;
import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class HazelcastServiceImpl implements HazelcastService {
    private HostInfo info;
    private MonitorChannel monitor;

    private HazelcastInstance hazelcast;

    public HazelcastServiceImpl(@Reference HostInfo info, @Monitor MonitorChannel monitor) {
        this.info = info;
        this.monitor = monitor;
    }

    @Init
    public void init() throws FileNotFoundException {
        Config config;
        File dir = info.getBaseDir();
        if (dir != null) {
            File configFile = new File(dir, "config" + File.separator + "hazelcast.xml");
            if (configFile.exists()) {
                XmlConfigBuilder builder = new XmlConfigBuilder(new FileInputStream(configFile));
                config = builder.build();
            } else {
                monitor.info("Hazelcast configuration not found in /config. Using default settings.");
                config = new XmlConfigBuilder().build();
            }

        } else {
            monitor.info("Hazelcast configuration not found in /config. Using default settings.");
            config = new XmlConfigBuilder().build();
        }
        MemberAttributeConfig attributeConfig = new MemberAttributeConfig();
        attributeConfig.setStringAttribute("key", getRuntimeKey());
        attributeConfig.setStringAttribute("zone", info.getZoneName());
        attributeConfig.setStringAttribute("runtime", info.getRuntimeName());
        config.setMemberAttributeConfig(attributeConfig);
        hazelcast = Hazelcast.newHazelcastInstance(config);

    }

    @Destroy
    public void destroy() {
        if (hazelcast != null) {
            hazelcast.shutdown();
        }
    }

    public HazelcastInstance getInstance() {
        return hazelcast;
    }

    private String getRuntimeKey() {
        return info.getDomain().toString() + ":" + info.getZoneName() + ":" + info.getRuntimeName();
    }

}
