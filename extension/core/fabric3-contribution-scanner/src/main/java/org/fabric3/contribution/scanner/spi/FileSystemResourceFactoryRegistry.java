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

import java.io.File;

/**
 * A registry for FileSystemResourceFactory instances.
 */
public interface FileSystemResourceFactoryRegistry {

    /**
     * Registers the factory.
     *
     * @param factory the factory to register
     */
    void register(FileSystemResourceFactory factory);

    /**
     * Creates a FileSystemResource for the given file by dispatching to a registered factory.
     *
     * @param file the file to create the FileSystemResource for
     * @return the FileSystemResource
     */
    FileSystemResource createResource(File file);
}
