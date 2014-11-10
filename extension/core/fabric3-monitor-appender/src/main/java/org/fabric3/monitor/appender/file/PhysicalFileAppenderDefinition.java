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
package org.fabric3.monitor.appender.file;

import org.fabric3.monitor.spi.model.physical.PhysicalAppenderDefinition;

/**
 * Configuration for a file appender.
 */
public class PhysicalFileAppenderDefinition extends PhysicalAppenderDefinition {
    private static final long serialVersionUID = 4239708371610267387L;

    private String fileName;
    private String rollType;
    private long rollSize;
    private int maxBackups;

    public PhysicalFileAppenderDefinition(String fileName, String rollType, long rollSize, int maxBackups) {
        this.fileName = fileName;
        this.rollType = rollType;
        this.rollSize = rollSize;
        this.maxBackups = maxBackups;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRollType() {
        return rollType;
    }

    public long getRollSize() {
        return rollSize;
    }

    public int getMaxBackups() {
        return maxBackups;
    }
}
