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
package org.fabric3.tx.atomikos.jms.connection;

import com.atomikos.jms.AtomikosConnectionFactoryBean;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * A wrapper used to expose an Atomikos {@link AtomikosConnectionFactoryBean} as a managed instance.
 *
 * @version $Rev$ $Date$
 */
@Management
public class ConnectionFactoryWrapper {
    private AtomikosConnectionFactoryBean delegate;

    public ConnectionFactoryWrapper(AtomikosConnectionFactoryBean delegate) {
        this.delegate = delegate;
    }

    @ManagementOperation(description = "The minimum connection pool size")
    public int getMinPoolSize() {
        return delegate.getMinPoolSize();
    }

    @ManagementOperation(description = "The minimum connection pool size")
    public void setMinPoolSize(int minPoolSize) {
        delegate.setMinPoolSize(minPoolSize);
    }

    @ManagementOperation(description = "The maximum connection pool size")
    public int getMaxPoolSize() {
        return delegate.getMaxPoolSize();
    }

    @ManagementOperation(description = "The maximum connection pool size")
    public void setMaxPoolSize(int maxPoolSize) {
        delegate.setMaxPoolSize(maxPoolSize);
    }

    @ManagementOperation(description = "Sets the connection pool minimum and maximum size")
    public void setPoolSize(int poolSize) {
        delegate.setMinPoolSize(poolSize);
        delegate.setMaxPoolSize(poolSize);
    }

    @ManagementOperation(description = "The maximum amount of time in seconds the pool will block waiting for a connection to become available in the pool when it is empty")
    public int getBorrowConnectionTimeout() {
        return delegate.getBorrowConnectionTimeout();
    }

    @ManagementOperation(description = "The maximum amount of time in seconds the pool will block waiting for a connection to become available in the pool when it is empty")
    public void setBorrowConnectionTimeout(int borrowConnectionTimeout) {
        delegate.setBorrowConnectionTimeout(borrowConnectionTimeout);
    }

    @ManagementOperation(description = "The amount of time in seconds the connection pool will allow a connection to be borrowed before claiming it back")
    public int getReapTimeout() {
        return delegate.getReapTimeout();
    }

    @ManagementOperation(description = "The amount of time in seconds the connection pool will allow a connection to be borrowed before claiming it back")
    public void setReapTimeout(int reapTimeout) {
        delegate.setReapTimeout(reapTimeout);
    }

    @ManagementOperation(description = "The maintenance interval for the pool maintenance thread")
    public void setMaintenanceInterval(int maintenanceInterval) {
        delegate.setMaintenanceInterval(maintenanceInterval);
    }

    @ManagementOperation(description = "The maintenance interval for the pool maintenance thread")
    public int getMaintenanceInterval() {
        return delegate.getMaintenanceInterval();
    }

    @ManagementOperation(description = "The maximum amount of time in seconds a connection can stay in the pool before being eligible for being closed during pool shrinking")
    public int getMaxIdleTime() {
        return delegate.getMaxIdleTime();
    }

    @ManagementOperation(description = "The maximum amount of time in seconds a connection can stay in the pool before being eligible for being closed during pool shrinking")
    public void setMaxIdleTime(int maxIdleTime) {
        delegate.setMaxIdleTime(maxIdleTime);
    }


}