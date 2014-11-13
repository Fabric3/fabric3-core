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
package org.fabric3.api.host;

import java.net.URI;

/**
 * Defines well-known names for contributions, component URIs and zones.
 */
public interface Names {

    String VERSION = "3.0.0";

    URI BOOT_CONTRIBUTION = URI.create("fabric3-boot");

    URI HOST_CONTRIBUTION = URI.create("fabric3-host");

    String RUNTIME_NAME = "fabric3://runtime";

    URI RUNTIME_URI = URI.create(RUNTIME_NAME);

    URI APPLICATION_DOMAIN_URI = URI.create(RUNTIME_NAME + "/ApplicationDomain");

    URI CONTRIBUTION_SERVICE_URI = URI.create(RUNTIME_NAME + "/ContributionService");

    URI MONITOR_FACTORY_URI = URI.create(RUNTIME_NAME + "/MonitorProxyService");

    URI RUNTIME_DOMAIN_SERVICE_URI = URI.create(RUNTIME_NAME + "/RuntimeDomain");

    URI NODE_DOMAIN_URI = URI.create(RUNTIME_NAME + "/Domain");

    String LOCAL_ZONE = "LocalZone";

    String DEFAULT_ZONE = "default.zone";
}
