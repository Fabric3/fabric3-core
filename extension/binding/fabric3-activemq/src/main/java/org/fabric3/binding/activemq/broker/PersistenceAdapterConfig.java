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
package org.fabric3.binding.activemq.broker;

/**
 * Encapsulates persistence adapter configuration for a broker.
 */
public class PersistenceAdapterConfig {
    private Long checkpointInterval;
    private Long cleanupInterval;
    private boolean disableLocking;
    private int indexBinSize;
    private int indexKeySize;
    private int indexPageSize;

    enum Type {
        AMQ, JDBC, JOURNAL, KAHA, MEMORY
    }

    private Type type;
    private boolean syncOnWrite;
    private String maxFileLength;


    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isSyncOnWrite() {
        return syncOnWrite;
    }

    public void setSyncOnWrite(boolean syncOnWrite) {
        this.syncOnWrite = syncOnWrite;
    }

    public String getMaxFileLength() {
        return maxFileLength;
    }

    public void setMaxFileLength(String maxFileLength) {
        this.maxFileLength = maxFileLength;
    }

    public Long getCheckpointInterval() {
        return checkpointInterval;
    }

    public void setCheckpointInterval(Long checkpointInterval) {
        this.checkpointInterval = checkpointInterval;
    }

    public Long getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(Long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public boolean isDisableLocking() {
        return disableLocking;
    }

    public void setDisableLocking(boolean disableLocking) {
        this.disableLocking = disableLocking;
    }

    public int getIndexBinSize() {
        return indexBinSize;
    }

    public void setIndexBinSize(int indexBinSize) {
        this.indexBinSize = indexBinSize;
    }

    public int getIndexKeySize() {
        return indexKeySize;
    }

    public void setIndexKeySize(int indexKeySize) {
        this.indexKeySize = indexKeySize;
    }

    public int getIndexPageSize() {
        return indexPageSize;
    }

    public void setIndexPageSize(int indexPageSize) {
        this.indexPageSize = indexPageSize;
    }
}
