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

package org.fabric3.tx.atomikos.jms.connection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.XAConnectionFactory;

import com.atomikos.jms.AtomikosConnectionFactoryBean;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.spi.runtime.ConnectionFactoryManager;
import org.fabric3.binding.jms.spi.runtime.FactoryRegistrationException;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;

/**
 * Initializes JMS connection factories with the Atomikos pooling infrastructure. Note, only XAConnections are supported but both XA and nonXA
 * transactions may be used.
 *
 * @version $Rev$ $Date$
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

    private Map<String, AtomikosConnectionFactoryBean> beans = new HashMap<String, AtomikosConnectionFactoryBean>();
    private Map<String, ConnectionFactory> nonXA = new HashMap<String, ConnectionFactory>();

    public AtomikosConnectionFactoryManager(@Reference ManagementService managementService) {
        this.managementService = managementService;
    }

    @Destroy
    public void destroy() {
        for (AtomikosConnectionFactoryBean bean : beans.values()) {
            try {
                remove(bean);
            } catch (ManagementException e) {
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

    public ConnectionFactory register(String name, ConnectionFactory factory) throws FactoryRegistrationException {
        return register(name, factory, Collections.<String, String>emptyMap());
    }

    public ConnectionFactory register(String name, ConnectionFactory factory, Map<String, String> properties) throws FactoryRegistrationException {

        if (!(factory instanceof XAConnectionFactory)) {
            if (nonXA.containsKey(name)) {
                throw new FactoryRegistrationException("Connection factory already exists: " + name);
            }
            nonXA.put(name, factory);
            return factory;
        }
        if (beans.containsKey(name)) {
            throw new FactoryRegistrationException("Connection factory already exists: " + name);
        }
        AtomikosConnectionFactoryBean bean = new AtomikosConnectionFactoryBean();
        bean.setUniqueResourceName(name);
        bean.setXaConnectionFactory((XAConnectionFactory) factory);
        String transactionMode = properties.get(TRANSACTION_MODE);
        bean.setLocalTransactionMode(Boolean.parseBoolean(transactionMode));
        String borrowTimeout = properties.get(BORROW_TIMEOUT);
        if (borrowTimeout != null) {
            try {
                bean.setBorrowConnectionTimeout(Integer.parseInt(borrowTimeout));
            } catch (NumberFormatException e) {
                throw new FactoryRegistrationException("Invalid connection borrow timeout for connection factory: " + name, e);
            }
        }
        String maintenance = properties.get(MAINTENANCE_INTERVAL);
        if (maintenance != null) {
            try {
                bean.setMaintenanceInterval(Integer.parseInt(maintenance));
            } catch (NumberFormatException e) {
                throw new FactoryRegistrationException("Invalid maintenance interval for connection factory: " + name, e);
            }
        }
        String maxIdle = properties.get(MAX_IDLE);
        if (maxIdle != null) {
            try {
                bean.setMaxIdleTime(Integer.parseInt(maxIdle));
            } catch (NumberFormatException e) {
                throw new FactoryRegistrationException("Invalid maximum idle time for connection factory: " + name, e);
            }
        }
        String maxPool = properties.get(MAX_POOL_SIZE);
        if (maxPool != null) {
            try {
                bean.setMaxPoolSize(Integer.parseInt(maxPool));
            } catch (NumberFormatException e) {
                throw new FactoryRegistrationException("Invalid maximum pool size for connection factory: " + name, e);
            }
        } else {
            bean.setMaxPoolSize(DEFAULT_MAX_POOL_SIZE);
        }
        String minPool = properties.get(MIN_POOL_SIZE);
        if (minPool != null) {
            try {
                bean.setMinPoolSize(Integer.parseInt(minPool));
            } catch (NumberFormatException e) {
                throw new FactoryRegistrationException("Invalid minimum pool size for connection factory: " + name, e);
            }
        }
        String pool = properties.get(POOL_SIZE);
        if (pool != null) {
            try {
                bean.setPoolSize(Integer.parseInt(pool));
            } catch (NumberFormatException e) {
                throw new FactoryRegistrationException("Invalid pool size for connection factory: " + name, e);
            }
        }

        String reap = properties.get(REAP_TIMEOUT);
        if (reap != null) {
            try {
                bean.setReapTimeout(Integer.parseInt(reap));
            } catch (NumberFormatException e) {
                throw new FactoryRegistrationException("Invalid reap timeout for connection factory: " + name, e);
            }
        }
        beans.put(name, bean);
        if (managementService != null) {
            ConnectionFactoryWrapper wrapper = new ConnectionFactoryWrapper(bean);
            try {
                managementService.export(name, JMS_XA_CONNECTION_POOLS, "Configured connection pool", wrapper);
            } catch (ManagementException e) {
                throw new FactoryRegistrationException("Error exporting " + name, e);
            }
        }
        return bean;
    }


    public void unregister(String name) throws FactoryRegistrationException {
        AtomikosConnectionFactoryBean bean = beans.remove(name);
        if (bean == null) {
            nonXA.remove(name);
        } else {
            try {
                remove(bean);
            } catch (ManagementException e) {
                throw new FactoryRegistrationException("Error exporting " + name, e);
            }
        }
    }

    private void remove(AtomikosConnectionFactoryBean bean) throws ManagementException {
        if (managementService != null) {
            String name = bean.getUniqueResourceName();
            managementService.remove(name, JMS_XA_CONNECTION_POOLS);
        }
    }


}