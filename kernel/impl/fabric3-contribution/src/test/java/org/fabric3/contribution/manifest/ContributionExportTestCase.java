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
package org.fabric3.contribution.manifest;

import java.net.URI;

import junit.framework.TestCase;

/**
 *
 */
public class ContributionExportTestCase extends TestCase {

    public void testEquals() throws Exception {
        URI uri = URI.create("contribution");
        ContributionExport export1 = new ContributionExport(uri);
        ContributionExport export2 = new ContributionExport(uri);
        assertEquals(export1, export2);
    }

    public void testMatch() throws Exception {
        URI uri = URI.create("contribution");
        ContributionExport export = new ContributionExport(uri);
        ContributionImport imprt = new ContributionImport(uri);
        assertTrue(export.match(imprt));

    }

}