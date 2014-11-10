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
package org.fabric3.spi.monitor;

import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 * Dispatch information for a monitor interface operation.
 */
public class DispatchInfo {
    private MonitorLevel level;
    private String message;

    public DispatchInfo(MonitorLevel level, String message) {
        this.level = level;
        this.message = message;
    }

    public MonitorLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Used to override the default message, e.g. when the default message needs to be localized.
     *
     * @param message the localized message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}