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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.runtime.bootstrap;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.runtime.HostInfo;

/**
 *
 */
public class ExtensionsScannerTestCase extends TestCase {
    private static final String BASE = "org/fabric3/fabric/runtime/bootstrap/repository/";

    private HostInfo info;

    public void testScan() throws Exception {
        ExtensionsScanner scanner = new ExtensionsScanner();
        List<ContributionSource> extensions = scanner.scan(info);
        assertEquals(1, extensions.size());
        EasyMock.verify(info);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File extensionsDir = new File(getClass().getClassLoader().getResource(BASE + "extensions/extension.jar").getFile()).getParentFile();
        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getExtensionsRepositoryDirectory()).andReturn(extensionsDir);

        EasyMock.replay(info);
    }
}