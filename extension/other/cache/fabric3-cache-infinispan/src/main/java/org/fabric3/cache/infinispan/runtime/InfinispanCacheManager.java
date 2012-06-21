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
package org.fabric3.cache.infinispan.runtime;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.management.MBeanServer;
import javax.transaction.TransactionManager;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.Parser;
import org.infinispan.jmx.MBeanServerLookup;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Element;

import org.fabric3.cache.infinispan.provision.InfinispanPhysicalResourceDefinition;
import org.fabric3.cache.infinispan.util.XmlHelper;
import org.fabric3.cache.spi.CacheBuildException;
import org.fabric3.cache.spi.CacheManager;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.federation.ZoneChannelException;

/**
 * Manages Infinispan cache resources on a runtime.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class InfinispanCacheManager implements CacheManager<InfinispanPhysicalResourceDefinition> {
    private TransactionManager tm;
    private MBeanServer mBeanServer;
    private HostInfo info;

    private Fabric3TransactionManagerLookup txLookup;

    private EmbeddedCacheManager cacheManager;
    private Map<String, Cache<?, ?>> caches = new ConcurrentHashMap<String, Cache<?, ?>>();
    private String channelConfig;

    @Property(required = false)
    public void setChannelConfig(Element config) throws TransformerException {
        Element element = (Element) config.getElementsByTagName("config").item(0);
        channelConfig = XmlHelper.transform(element);
    }

    public InfinispanCacheManager(@Reference TransactionManager tm, @Reference MBeanServer mBeanServer, @Reference HostInfo info) {
        this.tm = tm;
        this.mBeanServer = mBeanServer;
        this.info = info;
        txLookup = new Fabric3TransactionManagerLookup();
    }

    @Init
    public void init() throws ZoneChannelException {
        String authority = info.getDomain().getAuthority();
        GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder();
        configureCluster(builder);

        Fabric3MBeanServerLookup serverLookup = new Fabric3MBeanServerLookup();
        builder.globalJmxStatistics().jmxDomain(authority).mBeanServerLookup(serverLookup);

        this.cacheManager = new DefaultCacheManager(builder.build());
        cacheManager.start();
    }

    @Destroy
    public void destroy() throws ZoneChannelException {
        cacheManager.stop();
    }

    @SuppressWarnings({"unchecked"})
    public <CACHE> CACHE getCache(String name) {
        return (CACHE) caches.get(name);
    }

    public void create(InfinispanPhysicalResourceDefinition definition) throws CacheBuildException {
        // Set TCCL for the StAX parser use by Infinispan
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            ConfigurationBuilder builder = parseConfiguration(definition);

            builder.transaction().transactionManagerLookup(txLookup);

            Configuration configuration = builder.build();

            String cacheName = definition.getCacheName();
            cacheManager.defineConfiguration(cacheName, configuration);
            Cache<?, ?> cache = cacheManager.getCache(cacheName);
            caches.put(cacheName, cache);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void remove(InfinispanPhysicalResourceDefinition definition) throws CacheBuildException {
        String cacheName = definition.getCacheName();
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            Cache<?, ?> cache = caches.get(cacheName);
            if (cache == null) {
                throw new CacheBuildException("Cache not found: " + cacheName);
            }
            cache.stop();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private ConfigurationBuilder parseConfiguration(InfinispanPhysicalResourceDefinition definition) throws CacheBuildException {
        try {
            Parser parser = new Parser(this.getClass().getClassLoader());
            String configuration = definition.getCacheConfiguration();
            ByteArrayInputStream stream = new ByteArrayInputStream(configuration.getBytes());
            ConfigurationBuilderHolder holder = parser.parse(stream);
            return holder.newConfigurationBuilder(configuration);
        } catch (TransformerFactoryConfigurationError e) {
            throw new CacheBuildException(e);
        }
    }

    private void configureCluster(GlobalConfigurationBuilder builder) throws ZoneChannelException {
        // TODO support the case where a single VM is connecting to an external cache
        TransportConfigurationBuilder transportBuilder = builder.transport();
        transportBuilder.machineId(info.getRuntimeName());
        if (channelConfig != null) {
            transportBuilder.addProperty(JGroupsTransport.CONFIGURATION_XML, channelConfig);
        }
        JGroupsTransport transport = new JGroupsTransport();
        transportBuilder.transport(transport);
    }

    private class Fabric3MBeanServerLookup implements MBeanServerLookup {

        public MBeanServer getMBeanServer(Properties properties) {
            return mBeanServer;
        }
    }

    private class Fabric3TransactionManagerLookup implements TransactionManagerLookup {

        public TransactionManager getTransactionManager() throws Exception {
            return tm;
        }
    }


}




