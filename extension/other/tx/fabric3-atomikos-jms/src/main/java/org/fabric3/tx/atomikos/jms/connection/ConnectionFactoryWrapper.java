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

import com.atomikos.jms.AtomikosConnectionFactoryBean;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * A wrapper used to expose an Atomikos {@link AtomikosConnectionFactoryBean} as a managed instance.
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