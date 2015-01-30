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
package org.fabric3.api.host.runtime;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.fabric3.api.host.Environment;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.model.type.RuntimeMode;
import org.w3c.dom.Document;

/**
 * Provides operations to bootstrap a runtime.
 */
public interface BootstrapService {

    /**
     * Loads the system configuration value from a systemConfig.xml file or creates a default value if the file does not exist.
     *
     * @param configDirectory the directory where the file is located
     * @return the loaded value
     * @throws InitializationException if an error parsing the file contents is encountered
     */
    public Document loadSystemConfig(File configDirectory) throws InitializationException;

    /**
     * Returns a configuration property value for the runtime domain from the given source.
     *
     * @param source the source to read
     * @return the domain configuration property
     * @throws InitializationException if an error reading the source is encountered
     */
    Document loadSystemConfig(Source source) throws InitializationException;

    /**
     * Creates a default configuration property value for the runtime domain.
     *
     * @return a document representing the configuration property
     */
    Document createDefaultSystemConfig();

    /**
     * Returns the configured domain name the runtime should join. If not configured, the default domain name will be returned.
     *
     * @param systemConfig the system configuration
     * @return the domain name
     * @throws InitializationException if there is an error parsing the domain name
     */
    URI parseDomainName(Document systemConfig) throws InitializationException;

    /**
     * Returns the configured zone name. If not configured, the default zone name will be returned.
     *
     * @param systemConfig the system configuration
     * @param mode         the current runtime mode
     * @return the zone name
     * @throws InitializationException if there is an error parsing the zone name
     */
    String parseZoneName(Document systemConfig, RuntimeMode mode) throws InitializationException;

    /**
     * Returns the configured runtime mode. If not configured, {@link RuntimeMode#VM} will be returned.
     *
     * @param systemConfig the system configuration
     * @return the runtime mode
     * @throws InitializationException if there is an error parsing the runtime mode
     */
    public RuntimeMode parseRuntimeMode(Document systemConfig) throws InitializationException;

    /**
     * Returns the runtime environment. If one is not explicitly configured, the default {@link Environment#PRODUCTION} will be returned.
     *
     * @param systemConfig the system configuration
     * @return the parsed runtime environment
     */
    String parseEnvironment(Document systemConfig);

    /**
     * Returns configured deployment directories or an empty collection.
     *
     * @param systemConfig the system configuration
     * @return the deployment directories
     * @throws InitializationException if there is an error parsing the deployment directories
     */
    List<File> parseDeployDirectories(Document systemConfig) throws InitializationException;

    /**
     * Returns the product name. If one is not explicitly configured, "Fabric3" will be returned.
     *
     * @param systemConfig the system configuration
     * @return the parsed runtime environment
     * @throws InitializationException if there is an error parsing the product name
     */
    String parseProductName(Document systemConfig) throws InitializationException;

    /**
     * Returns the unique runtime name.
     *
     * @param domainName the domain name
     * @param zoneName   the zone name
     * @param runtimeId  the runtime id
     * @param mode       the runtime mode
     * @return the runtime name
     */
    String getRuntimeName(URI domainName, String zoneName, String runtimeId, RuntimeMode mode);

    /**
     * Introspects the contents of a file system repository and categorizes its contents as extensions or user contributions.
     *
     * @param info the host info
     * @return the result
     * @throws InitializationException if an error occurs during the scan operation
     */
    ScanResult scanRepository(HostInfo info) throws InitializationException;

    /**
     * Instantiates a default runtime implementation.
     *
     * @param configuration the base configuration for the runtime
     * @return the runtime instance
     */
    Fabric3Runtime createDefaultRuntime(RuntimeConfiguration configuration);

    /**
     * Creates default registrations for the runtime.
     *
     * @param runtime the runtime
     * @return default registrations
     */
    List<ComponentRegistration> createDefaultRegistrations(Fabric3Runtime runtime);

    /**
     * Instantiates a RuntimeCoordinator.
     *
     * @param configuration the configuration for the coordinator
     * @return the coordinator instance
     */
    RuntimeCoordinator createCoordinator(BootConfiguration configuration);

}