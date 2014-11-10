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
package org.fabric3.spi.introspection.xml;

/**
 *
 */
public class DeprecatedNamespaceHelper {

    private static final String BINDING = "urn:fabric3.org:binding";
    private static final String IMPLEMENTATION = "urn:fabric3.org:implementation";
    private static final String POLICY = "urn:fabric3.org:policy";
    private static final String OTHER = "urn:fabric3.org:other";

    /**
     * Returns true if the namespace has been deprecated.
     *
     * @param namespace the namespace
     * @return true if the namespace has been deprecated
     */
    public static boolean isDeprecatedNamespace(String namespace) {
        return BINDING.equals(namespace) || IMPLEMENTATION.equals(namespace) || POLICY.equals(namespace) || OTHER.equals(namespace);
    }

    private DeprecatedNamespaceHelper() {
    }
}