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
package org.fabric3.contribution.scanner.spi;

import java.net.URL;

/**
 * A resource on the file system such as a file or directory.
 */
public interface FileSystemResource {

    /**
     * Returns the resource name.
     *
     * @return the resource name
     */
    String getName();

    /**
     * Returns the resource location.
     *
     * @return the resource location
     */
    URL getLocation();

    /**
     * Returns the resource timestamp.
     *
     * @return the resource timestamp
     */
    long getTimestamp();

    /**
     * Returns the current state of the resource.
     *
     * @return the current state of the resource
     */
    FileSystemResourceState getState();

    /**
     * Sets the current state of the resource.
     *
     * @param state the resource state
     */
    void setState(FileSystemResourceState state);

    /**
     * Sets a check-point for tracking when the resource has changed.
     */
    public void checkpoint();

    /**
     * Returns true if the resource was modified since the last time it was check-pointed.
     *
     * @return true if the resource was modified since the last time it was check-pointed
     */
    boolean isChanged();

}
