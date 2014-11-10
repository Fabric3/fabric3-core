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
package org.fabric3.api.host.runtime;

/**
 * Tracks default packages visible in the system classloader that must be masked from the runtime boot classloader. This is required when the runtime
 * uses a version of a Java API that is different from the one provided by the JVM.
 */
public final class HiddenPackages {
    private static final String[] PACKAGES = new String[]{
            "javax.xml.bind.",
            "javax.xml.ws.",
            "com.sun.xml.jaxws.",
            "com.sun.xml.messaging.",
            "com.sun.xml.registry.",
            "com.sun.xml.rpc.",
            "com.sun.xml.security.",
            "com.sun.xml.stream.",
            "com.sun.xml.ws.",
            "com.sun.xml.wss.",
            "com.sun.xml.xwss.",
            "com.sun.xml.bind.",
            "org.springframework."
    };

    public static String[] getPackages() {
        return PACKAGES;
    }

    private HiddenPackages() {
    }
}