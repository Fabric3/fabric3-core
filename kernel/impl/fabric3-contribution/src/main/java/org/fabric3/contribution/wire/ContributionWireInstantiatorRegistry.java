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
package org.fabric3.contribution.wire;

import java.net.URI;

import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;

/**
 * A registry used to dispatch to ContributionWireInstantiators.  This is is required since the kernel does not support reinjection of multiplicity
 * references on Singleton components (it does, however, support reinjection of non-multiplicity references, which is done with this service).
 */
public interface ContributionWireInstantiatorRegistry {

    /**
     * Dispatches to the instantiator to create the wire.
     *
     * @param imprt     the wire's import
     * @param export    the wire's export
     * @param importUri the importing contribution URI
     * @param exportUri the exporting contribution URI
     * @param <I>       the import type
     * @param <E>       the export type
     * @return the ContributionWire
     */
    <I extends Import, E extends Export> ContributionWire<I, E> instantiate(I imprt, E export, URI importUri, URI exportUri);

}
