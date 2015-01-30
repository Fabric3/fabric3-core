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

package org.fabric3.tx.atomikos.jms.connection;

import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;
import java.util.HashMap;
import java.util.Map;

import com.atomikos.jms.AtomikosConnectionFactoryBean;
import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.management.ManagementService;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Initializes JMS connection factories with the Atomikos pooling infrastructure. Note, only XAConnections are supported but both XA and nonXA transactions may
 * be used.
 */
@EagerInit
public class AtomikosConnectionFactoryManager implements ConnectionFactoryManager {
    private static final String BORROW_TIMEOUT = "borrow.timeout";
    private static final String MAINTENANCE_INTERVAL = "maintenance.interval";
    private static final String MAX_IDLE = "max.idle";
    private static final String POOL_SIZE = "pool.size";
    private static final String MAX_POOL_SIZE = "pool.size.max";
    private static final String MIN_POOL_SIZE = "pool.size.min";
    private static final String REAP_TIMEOUT = "reap.timeout";
    private static final String TRANSACTION_MODE = "local.transaction.mode";
    private static final int DEFAULT_MAX_POOL_SIZE = 50;
    private static final String JMS_XA_CONNECTION_POOLS = "JMS/XA connection pools";

    private ManagementService managementService;

    private Map<String, AtomikosConnectionFactoryBean> beans = new HashMap<>();
    private Map<String, ConnectionFactory> nonXA = new HashMap<>();

    public AtomikosConnectionFactoryManager(@Reference ManagementService managementService) {
        this.managementService = managementService;
    }

    @Destroy
    public void destroy() {
        for (AtomikosConnectionFactoryBean bean : beans.values()) {
            try {
                remove(bean);
            } catch (ContainerException e) {
                // continue so the beans can be closed
                e.printStackTrace();
            }
        }
        for (AtomikosConnectionFactoryBean bean : beans.values()) {
            bean.close();
        }
        beans.clear();
    }

    public ConnectionFactory get(String name) {
        AtomikosConnectionFactoryBean bean = beans.get(name);
        if (bean != null) {
            return bean;
        }
        return nonXA.get(name);
    }

    public ConnectionFactory register(String name, ConnectionFactory factory, Map<String, String> properties) throws ContainerException {

        if (!(factory instanceof XAConnectionFactory)) {
            if (nonXA.containsKey(name)) {
                throw new ContainerException("Connection factory already exists: " + name);
            }
            nonXA.put(name, factory);
            return factory;
        }
        if (beans.containsKey(name)) {
            throw new ContainerException("Connection factory already exists: " + name);
        }

        AtomikosConnectionFactoryBean bean;
        // set TCCL as Atomikos uses it to load logging classes which are contained in its classloader (the current TCCL will be from the invoking thread and
        // may not have visibility)
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            bean = new AtomikosConnectionFactoryBean();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        bean.setUniqueResourceName(name);
        bean.setXaConnectionFactory((XAConnectionFactory) factory);
        String transactionMode = properties.get(TRANSACTION_MODE);
        bean.setLocalTransactionMode(Boolean.parseBoolean(transactionMode));
        String borrowTimeout = properties.get(BORROW_TIMEOUT);
        if (borrowTimeout != null) {
            try {
                bean.setBorrowConnectionTimeout(Integer.parseInt(borrowTimeout));
            } catch (NumberFormatException e) {
                throw new ContainerException("Invalid connection borrow timeout for connection factory: " + name, e);
            }
        }
        String maintenance = properties.get(MAINTENANCE_INTERVAL);
        if (maintenance != null) {
            try {
                bean.setMaintenanceInterval(Integer.parseInt(maintenance));
            } catch (NumberFormatException e) {
                throw new ContainerException("Invalid maintenance interval for connection factory: " + name, e);
            }
        }
        String maxIdle = properties.get(MAX_IDLE);
        if (maxIdle != null) {
            try {
                bean.setMaxIdleTime(Integer.parseInt(maxIdle));
            } catch (NumberFormatException e) {
                throw new ContainerException("Invalid maximum idle time for connection factory: " + name, e);
            }
        }
        String maxPool = properties.get(MAX_POOL_SIZE);
        if (maxPool != null) {
            try {
                bean.setMaxPoolSize(Integer.parseInt(maxPool));
            } catch (NumberFormatException e) {
                throw new ContainerException("Invalid maximum pool size for connection factory: " + name, e);
            }
        } else {
            bean.setMaxPoolSize(DEFAULT_MAX_POOL_SIZE);
        }
        String minPool = properties.get(MIN_POOL_SIZE);
        if (minPool != null) {
            try {
                bean.setMinPoolSize(Integer.parseInt(minPool));
            } catch (NumberFormatException e) {
                throw new ContainerException("Invalid minimum pool size for connection factory: " + name, e);
            }
        }
        String pool = properties.get(POOL_SIZE);
        if (pool != null) {
            try {
                bean.setPoolSize(Integer.parseInt(pool));
            } catch (NumberFormatException e) {
                throw new ContainerException("Invalid pool size for connection factory: " + name, e);
            }
        }

        String reap = properties.get(REAP_TIMEOUT);
        if (reap != null) {
            try {
                bean.setReapTimeout(Integer.parseInt(reap));
            } catch (NumberFormatException e) {
                throw new ContainerException("Invalid reap timeout for connection factory: " + name, e);
            }
        }
        beans.put(name, bean);
        if (managementService != null) {
            ConnectionFactoryWrapper wrapper = new ConnectionFactoryWrapper(bean);
            managementService.export(encodeName(name), JMS_XA_CONNECTION_POOLS, "Configured connection pool", wrapper);
        }
        return bean;
    }

    public ConnectionFactory unregister(String name) throws ContainerException {
        AtomikosConnectionFactoryBean bean = beans.remove(name);
        if (bean == null) {
            return nonXA.remove(name);
        } else {
            remove(bean);
            bean.close();
            return bean;
        }
    }

    private void remove(AtomikosConnectionFactoryBean bean) throws ContainerException {
        if (managementService != null) {
            String name = bean.getUniqueResourceName();
            managementService.remove(encodeName(name), JMS_XA_CONNECTION_POOLS);
        }
    }

    private String encodeName(String name) {
        return "transports/jms/pools/" + name.toLowerCase();
    }

}