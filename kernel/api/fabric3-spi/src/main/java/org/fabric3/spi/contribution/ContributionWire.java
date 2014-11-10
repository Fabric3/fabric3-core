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
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.net.URI;

/**
 * Represents a connection between a contribution import and a resolved contribution export. ContributionWire subtypes define specific semantics for
 * artifact visibility. For example, a Java-based wire may restrict visibility to a set of packages while a QName-based wire may restrict visibility
 * to a set of artifacts of a specified QName.
 */
public interface ContributionWire<I extends Import, E extends Export> extends Serializable {

    /**
     * Returns the import for this wire.
     *
     * @return the import for this wire
     */
    I getImport();

    /**
     * Returns the export this wire is mapped to.
     *
     * @return the export this wire is mapped to
     */
    E getExport();

    /**
     * Returns the importing contribution URI.
     *
     * @return the importing contribution URI
     */
    URI getImportContributionUri();

    /**
     * Returns the resolved exporting contribution URI.
     *
     * @return the resolved exporting contribution URI
     */
    URI getExportContributionUri();

    /**
     * Returns true if the wire resolves the resource.
     *
     * @param resource the Symbol representing the resource to resolve
     * @return true if the wire resolves the resource
     */
    boolean resolves(Symbol resource);

}
