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
package org.fabric3.transport.jetty.impl;

import java.io.IOException;

import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSocketConnector;

import org.fabric3.spi.threadpool.ExecutionContext;
import org.fabric3.spi.threadpool.ExecutionContextTunnel;

/**
 * Implementation that resets runtime execution statistics after an accept has completed as Jetty performs accepts in a continuous Runnable. If the
 * execution context were not reset, the thread executing the Runnable performing the accept may appear stuck to the runtime.
 *
 * @version $Rev: 7893 $ $Date: 2009-11-23 09:00:27 +0100 (Mon, 23 Nov 2009) $
 */
public class ContextAwareSslConnector extends SslSelectChannelConnector {

    @Override
    public void accept(int acceptorID) throws IOException {
        ExecutionContext context = ExecutionContextTunnel.getThreadExecutionContext();
        if (context == null) {
            // no execution context set, ignore
            super.accept(acceptorID);
        } else {
            try {
                context.start();
                super.accept(acceptorID);
                context.stop();
            } finally {
                context.clear();
            }
        }
    }
}