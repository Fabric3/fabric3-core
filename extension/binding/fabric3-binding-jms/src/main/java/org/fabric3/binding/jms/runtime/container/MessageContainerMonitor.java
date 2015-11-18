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
package org.fabric3.binding.jms.runtime.container;

import java.net.URI;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Severe;

/**
 *
 */
public interface MessageContainerMonitor {

    @Severe("All receivers are paused, possibly as a result of rejected work for {0}")
    void pauseError(String message);

    @Severe("The number is below the minimum threshold, possibly as a result of rejected work for {0}")
    void minimumError(String message);

    @Severe("Listener threw an exception for {0}")
    void listenerError(String uri, Throwable e);

    @Severe("Error unsubscribing {0}")
    void unsubscribeError(String uri, Throwable e);

    @Severe("Error refreshing connection for for {1}")
    void connectionError(String uri, Throwable e);

    @Severe("Error stopping connection for {0}")
    void stopConnectionError(URI uri, Throwable e);

    @Severe("Error receiving message for {0}")
    void receiveError(URI uri, Throwable e);

    @Debug("Receiver scheduled: {0}")
    void scheduledReceiver(String name);

    @Debug("Number of receivers increased to {0}")
    void increaseReceivers(int count);

    @Debug("Number of receivers decreased to {0}")
    void decreaseReceivers(int count);

    @Debug("Error starting connection {0}")
    void startConnectionError(Throwable e);

    @Debug("Work has been rejected with the following exception")
    void reject(Exception e);
}
