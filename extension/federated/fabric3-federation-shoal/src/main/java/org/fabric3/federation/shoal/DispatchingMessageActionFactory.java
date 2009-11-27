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
package org.fabric3.federation.shoal;

import com.sun.enterprise.ee.cms.core.Action;
import com.sun.enterprise.ee.cms.core.ActionException;
import com.sun.enterprise.ee.cms.core.MessageAction;
import com.sun.enterprise.ee.cms.core.MessageActionFactory;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.ee.cms.core.SignalAcquireException;
import com.sun.enterprise.ee.cms.core.SignalReleaseException;

/**
 * Dispatches to a callback handler.
 *
 * @version $Rev$ $Date$
 */
public class DispatchingMessageActionFactory implements MessageActionFactory {
    private String serviceName;
    private final FederationCallback callback;
    private ClassLoader loader;
    private FederationServiceMonitor monitor;

    public DispatchingMessageActionFactory(String serviceName, FederationCallback callback, ClassLoader loader, FederationServiceMonitor monitor) {
        this.serviceName = serviceName;
        this.callback = callback;
        this.loader = loader;
        this.monitor = monitor;
    }

    public Action produceAction() {
        return new DispatchingMessageAction();
    }

    private class DispatchingMessageAction implements MessageAction {

        public void consumeSignal(Signal signal) throws ActionException {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                // set the classloader so it is not the context classloader
                Thread.currentThread().setContextClassLoader(loader);
                signal.acquire();
                callback.onSignal(signal);
            } catch (SignalAcquireException e) {
                monitor.onException("Error acquiring signal", serviceName, e);
            } catch (FederationCallbackException e) {
                monitor.onException("Error processing signal", serviceName, e);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
                try {
                    signal.release();
                } catch (SignalReleaseException e) {
                    monitor.onException("Error releasing signal", serviceName, e);
                }
            }
        }
    }
}
