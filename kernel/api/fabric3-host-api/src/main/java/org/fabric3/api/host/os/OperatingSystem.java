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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.host.os;

import org.fabric3.api.host.Version;

/**
 * The current runtime operating system in normalized form.
 */
public class OperatingSystem {
    private String name;
    private String processor;
    private Version version;

    public OperatingSystem(String name, String processor, Version version) {
        this.name = name;
        this.processor = processor;
        this.version = version;
    }

    /**
     * Returns the normalized operating system name.
     *
     * @return the normalized operating system name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the normalized operating system processor.
     *
     * @return the normalized operating system processor
     */
    public String getProcessor() {
        return processor;
    }

    /**
     * Returns the operating system version.
     *
     * @return the operating system version
     */
    public Version getVersion() {
        return version;
    }


}
