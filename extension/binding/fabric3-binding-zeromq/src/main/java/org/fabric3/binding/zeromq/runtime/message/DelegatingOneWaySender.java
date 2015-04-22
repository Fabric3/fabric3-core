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
package org.fabric3.binding.zeromq.runtime.message;

import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.discovery.EntryChange;
import org.fabric3.spi.discovery.ServiceEntry;

/**
 * Delegates to another one-way sender.
 */
public class DelegatingOneWaySender implements OneWaySender {
    private String id;
    private DynamicOneWaySender delegate;
    private ZeroMQMetadata metadata;

    public DelegatingOneWaySender(String id, DynamicOneWaySender delegate, ZeroMQMetadata metadata) {
        this.id = id;
        this.delegate = delegate;
        this.metadata = metadata;
    }

    public void start() {
    }

    public void stop() {
    }

    public String getId() {
        return id;
    }

    public void accept(EntryChange change, ServiceEntry serviceEntry) {
        delegate.accept(change, serviceEntry);
    }

    public void send(byte[] message, int index, WorkContext context) {
        delegate.send(message, index, context, metadata);
    }
}
