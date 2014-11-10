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
package org.fabric3.runtime.weblogic.ds;

import java.util.HashSet;
import java.util.Set;
import javax.management.AttributeChangeNotification;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.datasource.spi.DataSourceRegistry;

import static org.fabric3.runtime.weblogic.api.Constants.WLS_RUNTIME_SERVICE_MBEAN;

/**
 * Resolves datasources configured in WebLogic via JMX and populates the {@link DataSourceRegistry}. Specifically, this implementation walks the
 * WebLogic MBean hierarchy:
 * <pre>
 *      RuntimeServiceMBean
 *          |
 *          ---DomainConfiguration
 *                  |
 *                  ---JDBCSystemResources
 *                        |
 *                        ---JDBCResource[]
 *                              |
 *                              ---JDBCDataSourceParams#JNDINames
 * <p/>
 * </pre>
 * The <code>JDBCResource</code> beans are iterated to determine the JNDI names where all system datasources are bound. The corresponding DataSource
 * instances are then resolved through JNDI and the Fabric3 DataSourceRegistry is populated.
 * <p/>
 * This implementation also dynamically updates the Fabric3 datasource registry if a configuration change is made to a live WebLogic domain or
 * runtime.
 * <p/>
 * Note that only system datasources will be resolved, not application-level datasources (i.e. those defined in Java EE modules).
 */
@EagerInit
public class DataSourceResolver {
    private DataSourceRegistry registry;
    private MBeanServer mbServer;
    private DataSourceResolverMonitor monitor;
    private Set<String> previousDataSources = new HashSet<>();

    public DataSourceResolver(@Reference DataSourceRegistry registry, @Reference MBeanServer mbServer, @Monitor DataSourceResolverMonitor monitor) {
        this.registry = registry;
        this.mbServer = mbServer;
        this.monitor = monitor;
    }

    @Init
    public void init() throws JMException, NamingException {
        updateDataSources(true);
    }

    /**
     * Resolves WebLogic datasources and updates the Fabric3 datasource registry. If a datasource exists in the registry, it will be overwritten.
     *
     * @param initialize true if the call is being made during server initialization
     * @throws NamingException if a JNDI connection cannot be established
     * @throws JMException     if there is an error looking up the JMX datasource mbeans
     */
    private void updateDataSources(boolean initialize) throws NamingException, JMException {
        InitialContext context = null;
        try {
            context = new InitialContext();
            ObjectName domainConfig = (ObjectName) mbServer.getAttribute(WLS_RUNTIME_SERVICE_MBEAN, "DomainConfiguration");
            ObjectName[] systemResources = (ObjectName[]) mbServer.getAttribute(domainConfig, "JDBCSystemResources");
            if (initialize) {
                // add a listener to be notified of changes
                mbServer.addNotificationListener(domainConfig, new DataSourceChangeListener(), null, null);
            }
            Set<String> newDataSources = new HashSet<>();
            for (ObjectName systemResource : systemResources) {
                ObjectName resource = (ObjectName) mbServer.getAttribute(systemResource, "JDBCResource");
                ObjectName params = (ObjectName) mbServer.getAttribute(resource, "JDBCDataSourceParams");
                String[] jndiNames = (String[]) mbServer.getAttribute(params, "JNDINames");
                registerDataSources(jndiNames, newDataSources, context);
            }
            for (String previous : previousDataSources) {
                if (!newDataSources.contains(previous)) {
                    monitor.removeDatasource(previous);
                    // datasource that was found previously was deleted - remove it from the registry
                    registry.unregister(previous);
                }
            }
            previousDataSources = newDataSources;
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    private void registerDataSources(String[] jndiNames, Set<String> newDataSources, InitialContext context) throws NamingException {
        for (String name : jndiNames) {
            try {
                DataSource dataSource = (DataSource) context.lookup(name);
                monitor.registerDatasource(name);
                newDataSources.add(name);
                registry.register(name, dataSource);
            } catch (NameNotFoundException e) {
                // This can happen if the datasource is configured on the admin server and not targeted to the current managed server
                // Issue a warning
                monitor.dataSourceNotFound(name);
            }
        }
    }

    /**
     * Listens for datasource configuration changes and updates the registry accordingly.
     */
    private class DataSourceChangeListener implements NotificationListener {

        public void handleNotification(Notification notification, Object handback) {
            if (!(notification instanceof AttributeChangeNotification)) {
                return;
            }
            AttributeChangeNotification change = (AttributeChangeNotification) notification;
            if (!"JDBCSystemResources".equals(change.getAttributeName())) {
                return;
            }
            try {
                updateDataSources(false);
            } catch (JMException | NamingException e) {
                monitor.error(e);
            }
        }
    }


}
