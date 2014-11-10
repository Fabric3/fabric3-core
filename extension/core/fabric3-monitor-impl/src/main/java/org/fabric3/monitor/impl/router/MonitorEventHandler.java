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

import com.lmax.disruptor.EventHandler;
import org.fabric3.monitor.spi.destination.MonitorDestinationRegistry;
import org.fabric3.monitor.spi.event.MonitorEventEntry;

/**
 * Receives events from the ring buffer and dispatches to the {@link MonitorDestinationRegistry}.
 */
public class MonitorEventHandler implements EventHandler<MonitorEventEntry> {
    //public static final int MIN = 100000;
    //public static final int MAX = 200000;

    private MonitorDestinationRegistry registry;

    //private int counter;
    //private long elapsedTime;

    public MonitorEventHandler(MonitorDestinationRegistry registry) {
        this.registry = registry;
    }

    public void onEvent(MonitorEventEntry entry, long sequence, boolean endOfBatch) throws Exception {
        entry.setEndOfBatch(endOfBatch);
        registry.write(entry);
        //        if (counter >= MIN) {
        //            long time = System.nanoTime() - entry.getTimestampNanos();
        //            elapsedTime = elapsedTime + time;
        //        }
        //        counter++;
        //        if (counter == MAX) {
        //            System.out.println("Time last event: " + (System.nanoTime() - entry.getTimestampNanos()));
        //            System.out.println("Elapsed: " + elapsedTime);
        //            System.out.println("Avg: " + (double) elapsedTime / (double) (MAX - MIN));
        //        }
    }

}
