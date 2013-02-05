/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.ScheduledFuture;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.implementation.java.runtime.JavaComponent;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.implementation.timer.provision.TimerData;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.federation.TopologyListener;
import org.fabric3.spi.federation.ZoneTopologyService;
import org.fabric3.timer.spi.Task;
import org.fabric3.timer.spi.TimerService;

/**
 * A timer component implementation.
 */
public class TimerComponent extends JavaComponent implements TopologyListener {
    private TimerData data;
    private Class<?> implementationClass;
    private TimerService timerService;
    private ScheduledFuture<?> future;
    private ZoneTopologyService topologyService;
    private InvokerMonitor monitor;
    private Scope scope;
    private HostInfo info;
    private ClassLoader classLoader;
    private TransactionManager tm;
    private boolean transactional;

    public TimerComponent(URI componentId,
                          QName deployable,
                          TimerData data,
                          Class<?> implementationClass,
                          boolean transactional,
                          ImplementationManagerFactory factory,
                          ScopeContainer scopeContainer,
                          TimerService timerService,
                          TransactionManager tm,
                          ZoneTopologyService topologyService,
                          HostInfo info,
                          InvokerMonitor monitor) {
        super(componentId, factory, scopeContainer, deployable, false);
        this.data = data;
        this.implementationClass = implementationClass;
        this.transactional = transactional;
        this.timerService = timerService;
        this.topologyService = topologyService;
        this.monitor = monitor;
        this.scope = scopeContainer.getScope();
        this.tm = tm;
        this.info = info;
        classLoader = factory.getImplementationClass().getClassLoader();
    }

    public void start() throws ComponentException {
        super.start();
        if (Scope.DOMAIN.equals(scope)) {
            if (topologyService != null) {
                topologyService.register(this);
            }
            if (RuntimeMode.PARTICIPANT == info.getRuntimeMode() && !topologyService.isZoneLeader()) {
                // defer scheduling until this node becomes zone leader
                return;
            }
        }
        schedule();
    }

    public void stop() throws ComponentException {
        super.stop();
        if (topologyService != null && Scope.DOMAIN.equals(scope)) {
            topologyService.deregister(this);
        }
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
    }

    public void onJoin(String name) {
        // no-op
    }

    public void onLeave(String name) {
        // no-op
    }

    public void onLeaderElected(String name) {
        if (!Scope.DOMAIN.equals(scope)) {
            return;
        }
        if (topologyService != null && !topologyService.isZoneLeader()) {
            // this runtime is not the leader, ignore
            return;
        }
        // this runtime was elected leader, schedule the components
        schedule();
    }

    private void schedule() {
        Runnable invoker;
        if (transactional) {
            invoker = new TransactionalTimerInvoker(this, tm, monitor);
        } else {
            invoker = new NonTransactionalTimerInvoker(this, monitor);
        }
        String name = data.getPoolName();
        long delay = data.getInitialDelay();

        switch (data.getType()) {
        case FIXED_RATE:
            future = timerService.scheduleAtFixedRate(name, invoker, delay, data.getFixedRate(), data.getTimeUnit());
            break;
        case INTERVAL:
            future = timerService.scheduleWithFixedDelay(name, invoker, delay, data.getRepeatInterval(), data.getTimeUnit());
            break;
        case RECURRING:
            scheduleRecurring(invoker);
            break;
        case ONCE:
            future = timerService.schedule(data.getPoolName(), invoker, data.getFireOnce(), data.getTimeUnit());
            break;
        }
    }

    private void scheduleRecurring(Runnable invoker) {
        try {
            Task task;
            if (data.isIntervalMethod()) {
                Method method = implementationClass.getMethod("nextInterval");
                if (transactional) {
                    task = new TransactionalIntervalTask(this, invoker, method, tm, monitor);
                } else {
                    task = new NonTransactionalIntervalTask(this, invoker, method, monitor);
                }
            } else {
                Object interval = classLoader.loadClass(data.getIntervalClass()).newInstance();
                task = new IntervalClassTask(interval, invoker);
            }
            future = timerService.scheduleRecurring(data.getPoolName(), task);
        } catch (NoSuchMethodException e) {
            monitor.executeError(e);
        } catch (InstantiationException e) {
            monitor.executeError(e);
        } catch (IllegalAccessException e) {
            monitor.executeError(e);
        } catch (ClassNotFoundException e) {
            monitor.executeError(e);
        }
    }

}
