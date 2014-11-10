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
package org.fabric3.plugin.api.runtime;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.fabric3.api.host.runtime.HostInfo;

/**
 * The host info type for the Gradle plugin runtime.
 */
public interface PluginHostInfo extends HostInfo {

    /**
     * Returns the URLs to project dependencies.
     *
     * @return the URLs to project dependencies.
     */
    Set<URL> getDependencyUrls();

    /**
     * Returns the build directory that contains contribution classes.
     *
     * @return the build directory
     */
    File getBuildDir();

    /**
     * Returns the directory containing compiled classes.
     *
     * @return the directory containing compiled classes
     */
    File getClassesDir();

    /**
     * Returns the directory containing compiled resources.
     *
     * @return the directory containing compiled resources
     */
    File getResourcesDir();

    /**
     * Returns the directory containing compiled test classes.
     *
     * @return the directory containing compiled test classes
     */
    File getTestClassesDir();

    /**
     * Returns the directory containing compiled test resources.
     *
     * @return the directory containing compiled test resources
     */
    File getTestResourcesDir();

}
