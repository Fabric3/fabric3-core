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
package org.fabric3.contribution.generator;

import java.net.URI;

import org.fabric3.contribution.wire.JavaContributionWire;
import org.fabric3.spi.contribution.ClassLoaderWireGenerator;
import org.fabric3.spi.model.physical.ClassLoaderWire;

/**
 * Generates a {@link ClassLoaderWire} that wires two classloaders for a Java package.
 */
public class JavaContributionWireGenerator implements ClassLoaderWireGenerator<JavaContributionWire> {

    public ClassLoaderWire generate(JavaContributionWire wire) {
        URI uri = wire.getExportContributionUri();
        String packageName = wire.getImport().getPackageInfo().getName();
        return new ClassLoaderWire(uri, packageName);
    }
}
