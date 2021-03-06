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
package org.fabric3.spi.transform;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Base interface for data format conversions.
 */
public interface Transformer<SOURCE, TARGET> {

    /**
     * Transforms the source instance into a new instance of the target type.
     *
     * @param source the source instance
     * @param loader the classloader for instantiating target types
     * @return a new instance of the target type
     * @throws Fabric3Exception if there was a problem during the transformation
     */
    TARGET transform(SOURCE source, ClassLoader loader) throws Fabric3Exception;

}
