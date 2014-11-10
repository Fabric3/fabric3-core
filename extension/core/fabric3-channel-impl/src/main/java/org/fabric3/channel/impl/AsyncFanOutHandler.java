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
package org.fabric3.channel.impl;

import java.util.concurrent.ExecutorService;

import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;

/**
 * Asynchronously broadcasts a received event to a collection of handlers.
 */
public class AsyncFanOutHandler extends AbstractFanOutHandler {
    private ExecutorService executorService;

    public AsyncFanOutHandler(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void handle(Object event, boolean endOfBatch) {
        if (connections.length == 0) {
            // no connections, skip scheduling work
            return;
        }
        FanOutWork work = new FanOutWork(event);
        executorService.execute(work);
    }

    private class FanOutWork implements Runnable {
        private Object event;

        private FanOutWork(Object event) {
            this.event = event;
        }

        public void run() {
            for (ChannelConnection connection : connections) {
                EventStream stream = connection.getEventStream();
                // force end of batch
                stream.getHeadHandler().handle(event, true);
            }
        }
    }
}
