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

import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.contribution.manifest.ContributionExport;
import org.fabric3.contribution.manifest.ContributionImport;

/**
 * Creates LocationContributionWireInstantiator instances.
 */
@EagerInit
public class LocationContributionWireInstantiator
        implements ContributionWireInstantiator<ContributionImport, ContributionExport, LocationContributionWire> {

    public LocationContributionWire instantiate(ContributionImport imprt, ContributionExport export, URI importUri, URI exportUri) {
        return new LocationContributionWire(imprt, export, importUri, exportUri);
    }

}