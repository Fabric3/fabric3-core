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
package org.fabric3.runtime.maven.itest;

import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.Element;

import org.fabric3.host.monitor.MonitorEvent;
import org.fabric3.host.monitor.MonitorEventDispatcher;

/**
 * Forwards monitor events to the Maven logger.
 *
 * @version $Rev$ $Date$
 */
public class MavenMonitorEventDispatcher implements MonitorEventDispatcher {
    private Log log;

    public MavenMonitorEventDispatcher(Log log) {
        this.log = log;
    }

    public void onEvent(MonitorEvent event) {
        Level level = event.getMonitorLevel();
        String message = event.getMessage();
        if (Level.SEVERE == level) {
            if (log.isErrorEnabled()) {
                Throwable e = null;
                for (Object o : event.getData()) {
                    if (o instanceof Throwable) {
                        e = (Throwable) o;
                    }
                }
                if (message != null) {
                    log.error(message, e);
                }
                log.error(e);
            }
        } else if (Level.WARNING == level) {
            if (log.isWarnEnabled()) {
                log.warn(message);
            }
        } else if (Level.INFO == level) {
            if (log.isInfoEnabled()) {
                log.info(message);
            }
        } else if (Level.FINE == level) {
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
        } else if (Level.FINER == level) {
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
        } else if (Level.FINEST == level) {
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
        }
    }

    public void configure(Element element) {
        // no-op
    }

    public void start() {
        // no-op
    }

    public void stop() {
        // no-op
    }

}