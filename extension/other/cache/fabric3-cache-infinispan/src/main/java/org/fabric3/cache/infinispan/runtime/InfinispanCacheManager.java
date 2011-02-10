/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

import org.fabric3.cache.infinispan.provision.InfinispanConfiguration;
import org.fabric3.cache.spi.CacheManager;
import org.fabric3.cache.spi.CacheRegistry;
import org.fabric3.host.Fabric3Exception;
import org.infinispan.config.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.manager.DefaultCacheManager;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

/**
 * Manages Infinispan caches on a runtime.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class InfinispanCacheManager implements CacheManager<InfinispanConfiguration> {

    private static DefaultCacheManager cacheManager;

    private CacheRegistry cacheRegistry;

    static {
        //TODO <michal.capo> not sure this will create singleton instance of cache manager due to classloading feature
        // we are just using the default configuration
        cacheManager = new DefaultCacheManager(GlobalConfiguration.getClusteredDefault());
        cacheManager.start();
    }

    public InfinispanCacheManager(@Reference CacheRegistry pCacheRegistry) {
        cacheRegistry = pCacheRegistry;
    }

    @Destroy
    public void stopManager() {
        cacheManager.stop();
    }

    public void create(InfinispanConfiguration configuration) throws Fabric3Exception {
        String config = "";
        try {
            Source source = new DOMSource(configuration.getCacheConfiguration());
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, result);
            config += writer.toString();
        } catch (TransformerConfigurationException e) {
            throw new InfinispanException("Problem during configuring the DefaultCacheManager for infinispan cache.", e);
        } catch (TransformerException e) {
            throw new InfinispanException("Problem during configuring the DefaultCacheManager for infinispan cache.", e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new InfinispanException("Problem during configuring the DefaultCacheManager for infinispan cache.", e);
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            /*
            * A Infinispan workaround for "org.infinispan.CacheException: Unable to construct a GlobalComponentRegistry!"
            *
            * This will help find classes.
            */
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            String cacheName = configuration.getCacheName();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(config.getBytes("UTF-8"));
            //TODO <michal.capo> at this time we are not including any configuration, due to jaxb class loading problem
            cacheManager.defineConfiguration(configuration.getCacheName(), new Configuration());
            cacheRegistry.register(cacheName, cacheManager.getCache(cacheName));
        } catch (UnsupportedEncodingException e) {
            throw new InfinispanException("Problem during configuring the DefaultCacheManager for infinispan cache.", e);
        } catch (IOException e) {
            throw new InfinispanException("Problem during configuring the DefaultCacheManager for infinispan cache.", e);
        } finally {
            // Set previously class loader back to thread
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public void remove(InfinispanConfiguration configuration) {
        String cacheName = configuration.getCacheName();

        cacheManager.startCache(cacheName);
        cacheRegistry.unregister(cacheName);
    }
}




