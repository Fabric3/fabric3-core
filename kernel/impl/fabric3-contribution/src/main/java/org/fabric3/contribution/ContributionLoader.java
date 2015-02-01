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
package org.fabric3.contribution;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.contribution.Contribution;

/**
 * Loads an installed contribution.
 */
public interface ContributionLoader {

    /**
     * Performs the load operation. This includes resolution of dependent contributions if necessary, and constructing a classloader with access to resources
     * contained in and required by the contribution.
     *
     * @param contribution the contribution to load
     * @return the classloader with access to the contribution and dependent resources
     * @throws Fabric3Exception if an error occurs during load
     */
    ClassLoader load(Contribution contribution) throws Fabric3Exception;

    /**
     * Unloads a contribution from memory.
     *
     * @param contribution the contribution to unload
     * @throws Fabric3Exception if the contribution cannot be unloaded because it is referenced by another loaded contribution
     */
    void unload(Contribution contribution) throws Fabric3Exception;
}
