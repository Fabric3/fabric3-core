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
package org.fabric3.monitor.impl.router;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Warning;

/**
 *
 */
public interface DestinationMonitor {

    @Info("Monitor is configured for synchronous output. Asynchronous output should be enabled on production systems.")
    void synchronousOutput();

    @Warning("Unknown ring buffer strategy configured for the monitor subsystem: {0}. Defaulting to blocking strategy.")
    void invalidStrategy(String strategy);

    @Warning("The monitor subsystem was configured with an unknown mode: {0}. Defaulting to development mode.")
    void unknownMode(String mode);

    @Debug("Blocking strategy enabled on monitor ring buffer")
    void blockingStrategy();

    @Debug("Yielding strategy enabled on monitor ring buffer")
    void yieldingStrategy();

    @Debug("Sleeping wait strategy enabled on monitor ring buffer")
    void sleepingStrategy();

    @Debug("Phased backoff with lock strategy enabled on monitor ring buffer. Spin timeout (ns): {0}. Yield timeout (ns): {1}.")
    void phasedBackoffWithLockStrategy(long spinTimeoutNanos, long yieldTimeoutNanos);

    @Debug("Phased backoff with sleep strategy enabled on monitor ring buffer. Spin timeout (ns): {0}. Yield timeout (ns): {1}.")
    void phasedBackoffWithSleepStrategy(long spinTimeoutNanos, long yieldTimeoutNanos);

    @Debug("Busy spin strategy enabled on monitor ring buffer")
    void busySpinStrategy();

    @Debug("Timeout strategy enabled on monitor ring buffer. Timeout (ns): {0}.")
    void timeoutStrategy(long timeoutNanos);

}
