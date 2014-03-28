/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
