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
 * Implementations instantiate a ContributionWire between a contribution import and a resolved export from another contribution.
 */
public interface ContributionWireInstantiator<I extends Import, E extends Export, CW extends ContributionWire<I, E>> {

    /**
     * Instantiates the wire.
     *
     * @param imprt     the import
     * @param export    the resolved export
     * @param importUri the URI of the contribution containing the import
     * @param exportUri the URI of the contribution containing the export
     * @return the ContributionWire
     */
    CW instantiate(I imprt, E export, URI importUri, URI exportUri);

}
