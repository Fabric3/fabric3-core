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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.timer.runtime;

import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.ScheduledFuture;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.implementation.timer.model.TimerData;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Scope;
import org.fabric3.implementation.java.runtime.JavaComponent;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactory;
import org.fabric3.spi.container.component.ScopeContainer;
import org.fabric3.spi.federation.topology.NodeTopologyService;
import org.fabric3.spi.federation.topology.TopologyListener;
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
    private NodeTopologyService topologyService;
    private InvokerMonitor monitor;
    private boolean scheduleOnStart;
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
                          NodeTopologyService topologyService,
                          HostInfo info,
                          InvokerMonitor monitor,
                          boolean scheduleOnStart) {
        super(componentId, factory, scopeContainer, deployable, false);
        this.data = data;
        this.implementationClass = implementationClass;
        this.transactional = transactional;
        this.timerService = timerService;
        this.topologyService = topologyService;
        this.monitor = monitor;
        this.scheduleOnStart = scheduleOnStart;
        this.scope = scopeContainer.getScope();
        this.tm = tm;
        this.info = info;
        classLoader = factory.getImplementationClass().getClassLoader();
    }

    public void start() throws Fabric3Exception {
        super.start();
        if (Scope.DOMAIN.equals(scope)) {
            if (topologyService != null) {
                topologyService.register(this);
            }
            if (RuntimeMode.NODE == info.getRuntimeMode() && !topologyService.isZoneLeader()) {
                // defer scheduling until this node becomes zone leader
                return;
            }
        }
        if (scheduleOnStart) {
            // only schedule on start if the runtime has started. If the runtime has not yet started, {@link #schedule} will be called externally on start.
            schedule();
        }
    }

    public void stop() throws Fabric3Exception {
        super.stop();
        if (topologyService != null && Scope.DOMAIN.equals(scope)) {
            topologyService.deregister(this);
        }
        if (future != null && !future.isCancelled() && !future.isDone()) {
            future.cancel(true);
        }
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

    public void schedule() {
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
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            monitor.executeError(e);
        }
    }

}
