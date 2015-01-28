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
package org.fabric3.api.host.runtime;

import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.host.Names;

/**
 * Returns the packages that should be exported by the boot contribution.
 */
public final class BootExports {
    private static final Map<String, String> BOOT_EXPORTS;

    static {

        BOOT_EXPORTS = new HashMap<>();

        // Fabric3 packages
        BOOT_EXPORTS.put("org.fabric3.api", Names.VERSION);
        BOOT_EXPORTS.put("org.fabric3.api.annotation", Names.VERSION);
        BOOT_EXPORTS.put("org.fabric3.api.host.*", Names.VERSION);
        BOOT_EXPORTS.put("org.fabric3.api.model.*", Names.VERSION);
        BOOT_EXPORTS.put("org.fabric3.api.node.*", Names.VERSION);
        BOOT_EXPORTS.put("org.fabric3.spi.*", Names.VERSION);
        BOOT_EXPORTS.put("org.fabric3.util.*", Names.VERSION);
        BOOT_EXPORTS.put("org.fabric3.management.*", Names.VERSION);
        BOOT_EXPORTS.put("org.fabric3.implementation.pojo.*", Names.VERSION);

    }

    private BootExports() {
    }

    public static Map<String, String> getExports() {
        return BOOT_EXPORTS;
    }

    public static void addExport(String pkg, String version) {
        BOOT_EXPORTS.put(pkg, version);
    }

}