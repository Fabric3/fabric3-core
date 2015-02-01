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
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class ContributionWireInstantiatorRegistryImpl implements ContributionWireInstantiatorRegistry {
    private Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators = new HashMap<>();

    @Reference
    public void setInstantiators(Map<Class<? extends Import>, ContributionWireInstantiator<?, ?, ?>> instantiators) {
        this.instantiators = instantiators;
    }

    public <I extends Import, E extends Export> ContributionWire<I, E> instantiate(I imprt, E export, URI importUri, URI exportUri) {
        ContributionWireInstantiator instantiator = instantiators.get(imprt.getClass());
        if (instantiator == null) {
            throw new AssertionError("Instantiator not configured: " + imprt.getClass());
        }
        // cast is safe
        //noinspection unchecked
        return instantiator.instantiate(imprt, export, importUri, exportUri);

    }

}
