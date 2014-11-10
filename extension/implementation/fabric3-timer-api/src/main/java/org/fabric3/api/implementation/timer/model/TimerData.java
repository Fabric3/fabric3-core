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
package org.fabric3.api.implementation.timer.model;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * Schedule metadata for a timer component.
 */
public class TimerData implements Serializable {
    private static final long serialVersionUID = 5814910790533612455L;

    public static final String DEFAULT_POOL = "default";

    public static final long UNSPECIFIED = -1;

    private TimerType type = TimerType.INTERVAL;
    private String poolName = DEFAULT_POOL;
    private String intervalClass;
    private boolean intervalMethod;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private long initialDelay = 100;
    private long fixedRate = UNSPECIFIED;
    private long repeatInterval = UNSPECIFIED;
    private long fireOnce = UNSPECIFIED;

    public TimerType getType() {
        return type;
    }

    public void setType(TimerType type) {
        this.type = type;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public long getInitialDelay() {
        return initialDelay;
    }

    public void setInitialDelay(long initialDelay) {
        this.initialDelay = initialDelay;
    }

    public long getFixedRate() {
        return fixedRate;
    }

    public void setFixedRate(long fixedRate) {
        this.fixedRate = fixedRate;
    }

    public long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public long getFireOnce() {
        return fireOnce;
    }

    public void setFireOnce(long fireOnce) {
        this.fireOnce = fireOnce;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getIntervalClass() {
        return intervalClass;
    }

    public void setIntervalClass(String intervalClass) {
        this.intervalClass = intervalClass;
    }

    public boolean isIntervalMethod() {
        return intervalMethod;
    }

    public void setIntervalMethod(boolean intervalMethod) {
        this.intervalMethod = intervalMethod;
    }
}
