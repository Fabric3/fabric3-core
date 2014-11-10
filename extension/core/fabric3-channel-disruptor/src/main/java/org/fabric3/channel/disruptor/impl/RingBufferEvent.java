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
package org.fabric3.channel.disruptor.impl;

import org.fabric3.api.ChannelEvent;

/**
 * A holder for events sent through a channel ring buffer.
 */
public class RingBufferEvent implements ChannelEvent {
    private Object event;
    private Object parsed;
    private boolean endOfBatch;
    private long sequence =-1;

    public <T> T getEvent(Class<T> type) {
        return type.cast(event);
    }

    public void setEvent(Object event) {
        this.event = event;
    }

    public <T> T getParsed(Class<T> type) {
        return type.cast(parsed);
    }

    public void setParsed(Object parsed) {
        this.parsed = parsed;
    }

    public boolean isEndOfBatch() {
        return endOfBatch;
    }

    public void setEndOfBatch(boolean endOfBatch) {
        this.endOfBatch = endOfBatch;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}