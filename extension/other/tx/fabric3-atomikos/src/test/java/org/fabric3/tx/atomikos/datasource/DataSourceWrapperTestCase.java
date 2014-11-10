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
package org.fabric3.tx.atomikos.datasource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Collections;
import java.util.logging.Logger;

import com.atomikos.datasource.pool.ConnectionFactory;
import com.atomikos.jdbc.AbstractDataSourceBean;
import junit.framework.TestCase;

/**
 *
 */
public class DataSourceWrapperTestCase extends TestCase {

    public void testWrapper() throws Exception {
        AbstractDataSourceBean bean = new MockBean();
        DataSourceWrapper wrapper = new DataSourceWrapper(bean, Collections.<String>emptyList());
        wrapper.setBorrowConnectionTimeout(1000);
        wrapper.setLoginTimeout(10);
        wrapper.setMaintenanceInterval(20);
        wrapper.setMaxIdleTime(30);
        wrapper.setMaxPoolSize(50);
        wrapper.setMinPoolSize(40);
        wrapper.setReapTimeout(70);
        wrapper.setTestQuery("test query");

        assertEquals(1000, wrapper.getBorrowConnectionTimeout());
        assertEquals(10, wrapper.getLoginTimeout());
        assertEquals(20, wrapper.getMaintenanceInterval());
        assertEquals(30, wrapper.getMaxIdleTime());
        assertEquals(50, wrapper.getMaxPoolSize());
        assertEquals(40, wrapper.getMinPoolSize());
        assertEquals(70, wrapper.getReapTimeout());
        assertEquals("test query", wrapper.getTestQuery());

        wrapper.setPoolSize(60);
        assertEquals(60, wrapper.getMinPoolSize());
        assertEquals(60, wrapper.getMaxPoolSize());

    }

    private class MockBean extends AbstractDataSourceBean {
        private static final long serialVersionUID = 5931333715307099716L;

        @Override
        protected ConnectionFactory doInit() throws Exception {
            return null;
        }

        @Override
        protected void doClose() {

        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        protected Object unwrapVendorInstance() {
            return null;
        }

        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        protected boolean isAssignableFromWrappedVendorClass(Class<?> aClass) {
            return false;
        }

        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return null;
        }
    }
}
