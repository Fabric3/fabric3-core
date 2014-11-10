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
import javax.xml.namespace.QName;

/**
 * A contribution export.
 */
public interface Export extends Serializable {

    /**
     * Returns the export type.
     *
     * @return the export type
     */
    QName getType();

    /**
     * Returns true if an import matched the export.
     *
     * @param imprt the import to match
     * @return true if an import matched the export
     */
    boolean match(Import imprt);

    /**
     * True if this export has been resolved to an import. When resolving an import, previously resolved exports must be preferred over unresolved
     * ones.
     *
     * @return true if this export has been resolved to an import
     */
    boolean isResolved();

    /**
     * Marks the export as resolved for an import.
     */
    void resolve();
}
