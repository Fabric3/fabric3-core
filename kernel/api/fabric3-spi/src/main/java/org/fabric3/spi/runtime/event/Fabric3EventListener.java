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
 * Implementations are notified of runtime events after they have subscribed with the {@link EventService} for a particular event type or types.
 */
public interface Fabric3EventListener<T extends Fabric3Event> {

    /**
     * Notifies the listener of an event. The listener must not throw an exception as all listeners are notified on the same thread.
     *
     * @param event the event
     */
    void onEvent(T event);

}
