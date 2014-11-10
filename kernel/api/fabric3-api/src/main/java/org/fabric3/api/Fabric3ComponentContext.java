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
package org.fabric3.api;

import java.io.File;
import java.net.URI;

import org.oasisopen.sca.ComponentContext;

/**
 * A Fabric3 extension to the OASIS SCA ComponentContext API. Components may reference this interface when for fields or setters marked with @Context instead of
 * the SCA RequestContext variant. For example:
 * <pre>
 * public class SomeComponent implements SomeService {
 *      &#064;Context
 *      protected Fabric3ComponentContext context;
 *      //...
 * }
 * </pre>
 * At runtime, the <code>context</code> field will be injected with an instance of Fabric3ComponentContext.
 */
public interface Fabric3ComponentContext extends ComponentContext {

    /**
     * Returns the unique name associated with this runtime. Names survive restarts.
     *
     * @return the unique runtime name
     */
    String getRuntimeName();

    /**
     * Returns the SCA domain associated with this runtime.
     *
     * @return the SCA domain associated with this runtime
     */
    URI getDomain();

    /**
     * Returns the runtime environment type.
     *
     * @return the runtime environment type
     */
    String getEnvironment();

    /**
     * Returns the directory where persistent data can be written.
     *
     * @return the directory where persistent data can be written or null if the runtime does not support persistent capabilities
     */
    File getDataDirectory();

    /**
     * Returns the temporary directory.
     *
     * @return the temporary directory.
     */
    File getTempDirectory();

}
