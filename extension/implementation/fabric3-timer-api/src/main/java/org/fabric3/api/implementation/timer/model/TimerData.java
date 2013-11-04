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
