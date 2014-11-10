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
package org.fabric3.binding.web.log;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;

/**
 *
 */
@EagerInit
public class LogLevelSetter {
    private Level logLevel = Level.WARNING;

    @Property(required = false)
    public void setMonitorLevel(String logLevel) {
        this.logLevel = Level.parse(logLevel);
    }

    @Init
    public void init() {
        // Atmosphere default level is INFO which is verbose. Only log warnings by default
        Logger.getLogger("Atmosphere").setLevel(logLevel);
    }
}
