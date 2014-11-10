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
 */
package org.fabric3.monitor.impl.router;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Warning;

/**
 *
 */
public interface DestinationMonitor {

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
