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
package org.fabric3.spi.runtime.event;

/**
 * The runtime event service. {@link Fabric3EventListener}s subscribe with this service to receive notification of various runtime events.
 */
public interface EventService {

    /**
     * Publishes a runtime event. EventListeners subscribed to the event will be notified.
     *
     * @param event the event
     */
    void publish(Fabric3Event event);

    /**
     * Subscribe the listener to receive notification when events of the specified type are published.
     *
     * @param type     the event type to receive notifications for
     * @param listener the listener to subscribe
     */
    <T extends Fabric3Event> void subscribe(Class<T> type, Fabric3EventListener<T> listener);

    /**
     * Unsubscribe the listener from receiving notifications when events of the specified type are published.
     *
     * @param type     the event type to unsubscribe from
     * @param listener the listener to unsubscribe
     */
    <T extends Fabric3Event> void unsubscribe(Class<T> type, Fabric3EventListener<T> listener);


}
