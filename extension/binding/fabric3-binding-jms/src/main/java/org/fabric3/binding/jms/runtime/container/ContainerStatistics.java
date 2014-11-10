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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks statistics for an {@link AdaptiveMessageContainer}.
 */
public class ContainerStatistics {
    private long start;
    private AtomicLong messagesReceived = new AtomicLong();
    private AtomicInteger maxReceivers = new AtomicInteger();
    private AtomicInteger transactions = new AtomicInteger();
    private AtomicInteger transactionsRolledBack = new AtomicInteger();

    public ContainerStatistics() {
        start = System.currentTimeMillis();
    }

    public long getTotalTime() {
        return System.currentTimeMillis() - start;
    }

    public long getMessagesReceived() {
        return messagesReceived.get();
    }

    public void incrementMessagesReceived() {
        messagesReceived.incrementAndGet();
    }

    public int getMaxReceivers() {
        return maxReceivers.get();
    }

    public void incrementMaxReceivers() {
        maxReceivers.incrementAndGet();
    }

    public int getTransactions() {
        return transactions.get();
    }

    public void incrementTransactions() {
        transactions.incrementAndGet();
    }

    public int getTransactionsRolledBack() {
        return transactionsRolledBack.get();
    }

    public void incrementTransactionsRolledBack() {
        transactionsRolledBack.incrementAndGet();
    }
}