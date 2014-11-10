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
package org.fabric3.plugin.runtime;

import java.net.URI;

/**
 * Constants used by plugin runtimes.
 */
public interface PluginConstants {

    /**
     * The default domain name for tests.
     */
    String DOMAIN = "fabric3://domain";

    /**
     * The name for the user-provided latch service that services a runtime synchronization mechanism when performing integration tests that require
     * asynchronous communication.
     */
    URI TEST_LATCH_SERVICE = URI.create(DOMAIN + "/F3LatchService");

    /**
     * The JUnit version.
     */
    String JUNIT_VERSION = "4.11";

}
