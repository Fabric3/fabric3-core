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
package org.fabric3.implementation.timer.runtime;

import java.net.URI;
import java.text.ParseException;
import java.util.concurrent.ScheduledFuture;
import javax.xml.namespace.QName;

import org.fabric3.implementation.java.runtime.JavaComponent;
import org.fabric3.implementation.pojo.instancefactory.InstanceFactoryProvider;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.implementation.timer.provision.TriggerData;
import org.fabric3.timer.spi.TimerService;

/**
 * A timer component implementation.
 *
 * @version $Rev: 7881 $ $Date: 2009-11-22 10:32:23 +0100 (Sun, 22 Nov 2009) $
 */
public class TimerComponent<T> extends JavaComponent<T> {
    private TriggerData data;
    private TimerService timerService;
    private ScheduledFuture<?> future;

    /**
     * Constructor for a timer component.
     *
     * @param componentId             the component's uri
     * @param instanceFactoryProvider the provider for the instance factory
     * @param scopeContainer          the container for the component's implementation scope
     * @param deployable              the deployable composite this component is deployed with Ê
     * @param eager                   true if the component should be eager initialized
     * @param maxIdleTime             the time after which idle instances of this component can be expired
     * @param maxAge                  the time after which instances of this component can be expired
     * @param data                    timer fire data
     * @param timerService            the timer service
     */
    public TimerComponent(URI componentId,
                          InstanceFactoryProvider<T> instanceFactoryProvider,
                          ScopeContainer scopeContainer,
                          QName deployable,
                          boolean eager,
                          long maxIdleTime,
                          long maxAge,
                          TriggerData data,
                          TimerService timerService) {
        super(componentId,
              instanceFactoryProvider,
              scopeContainer,
              deployable,
              eager,
              maxIdleTime,
              maxAge
        );
        this.data = data;
        this.timerService = timerService;
    }

    public void start() throws ComponentException {
        super.start();
        TimerComponentInvoker<T> invoker = new TimerComponentInvoker<T>(this);
        switch (data.getType()) {
        case CRON:
            try {
                future = timerService.schedule(invoker, data.getCronExpression());
            } catch (ParseException e) {
                // this should be caught on the controller
                throw new TimerComponentInitException(e);
            }
            break;
        case FIXED_RATE:
            throw new UnsupportedOperationException("Not yet implemented");
            // break;
        case INTERVAL:
            future = timerService.scheduleWithFixedDelay(invoker, data.getStartTime(), data.getRepeatInterval(), data.getTimeUnit());
            break;
        case ONCE:
            future = timerService.schedule(invoker, data.getFireOnce(), data.getTimeUnit());
            break;
        }
    }

    public void stop() throws ComponentException {
        super.stop();
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
    }


}
