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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.webapp;

/**
 * Configuration paramterts used by the web application runtime bootstrap process.
 *
 * @version $Rev$ $Date$
 */
public final class Constants {

    /**
     * Default management domain.
     */
    public static final String DEFAULT_MANAGEMENT_DOMAIN = "webapp-host";

    /**
     * Name of the servlet context-param that should contain the JMX management domain.
     */
    public static final String MANAGEMENT_DOMAIN_PARAM = "fabric3.management.domain";

    /**
     * Name of the servlet context-param that should contain the component id for the webapp.
     */
    public static final String DOMAIN_PARAM = "fabric3.domain";

    /**
     * Name of the servlet context-param that should contain the component target namespace for the webapp.
     */
    public static final String COMPOSITE_NAMESPACE_PARAM = "fabric3.compositeNamespace";

    /**
     * Name of the servlet context-param that should contain the component id for the webapp.
     */
    public static final String COMPOSITE_PARAM = "fabric3.composite";

    /**
     * Name of the servlet context-param that should contain the component id for the webapp.
     */
    public static final String COMPONENT_PARAM = "fabric3.component";

    /**
     * Name of the servlet context-param that should contain the token separated list of policy files.
     */
    public static final String POLICY_PARAM = "fabric3.policy";

    /**
     * Servlet context-param name for user-specified application SCDL path.
     */
    public static final String APPLICATION_COMPOSITE_PATH_PARAM = "fabric3.applicationCompositePath";

    /**
     * Default application composite path.
     */
    public static final String APPLICATION_COMPOSITE_PATH_DEFAULT = "/WEB-INF/web.composite";

    /**
     * Name of the context attribute that contains the ComponentContext.
     */
    public static final String CONTEXT_ATTRIBUTE = "org.osoa.sca.ComponentContext";

    /**
     * Name of the context attribute that contains the ComponentContext.
     */
    public static final String OASIS_CONTEXT_ATTRIBUTE = "org.oasisopen.sca.ComponentContext";

    /**
     * Name of the parameter that defines whether the work scheduler should pause on start.
     */
    public static final String PAUSE_ON_START_PARAM = "fabric3.work.scheduler.pauseOnStart";

    /**
     * The default pause on start value.
     */
    public static final String PAUSE_ON_START_DEFAULT = "false";

    /**
     * Name of the parameter that defines the number of worker threads.
     */
    public static final String NUM_WORKERS_PARAM = "fabric3.work.scheduler.numWorkers";

    /**
     * The number of default worker threads.
     */
    public static final String NUM_WORKERS_DEFAULT = "10";

    /**
     * Context attribute to which the Fabric3 runtime for this servlet context is stored.
     */
    public static final String RUNTIME_ATTRIBUTE = "fabric3.runtime";

    /**
     * Name of the parameter that defines the class to load to launch the runtime.
     */
    public static final String MONITOR_FACTORY_PARAM = "fabric3.monitorFactory";

    /**
     * Name of the default webapp runtime implementation.
     */
    public static final String MONITOR_FACTORY_DEFAULT = "org.fabric3.monitor.impl.JavaLoggingMonitorFactory";

    /**
     * Default monitor configuration file path found in the webapp.
     */
    public static final String MONITOR_CONFIG_PATH = "/WEB-INF/monitor.properties";

    private Constants() {
    }
}
