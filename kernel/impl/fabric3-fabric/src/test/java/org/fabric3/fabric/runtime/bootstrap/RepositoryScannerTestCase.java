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

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.host.runtime.ScanResult;

/**
 *
 */
public class RepositoryScannerTestCase extends TestCase {
    private static final String BASE = "org/fabric3/fabric/runtime/bootstrap/repository/";

    private HostInfo info;

    public void testScan() throws Exception {
        RepositoryScanner scanner = new RepositoryScanner();
        ScanResult result = scanner.scan(info);
        assertEquals(2, result.getExtensionContributions().size());
        assertEquals(1, result.getUserContributions().size());
        EasyMock.verify(info);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File extensionsDir = new File(getClass().getClassLoader().getResource(BASE + "extensions/extension.jar").getFile()).getParentFile();
        File runtimeDir = new File(getClass().getClassLoader().getResource(BASE + "runtime/runtime.jar").getFile()).getParentFile();
        File userDir = new File(getClass().getClassLoader().getResource(BASE + "user/user.jar").getFile()).getParentFile();
        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getExtensionsRepositoryDirectory()).andReturn(extensionsDir);
        EasyMock.expect(info.getRuntimeRepositoryDirectory()).andReturn(runtimeDir);
        EasyMock.expect(info.getUserRepositoryDirectory()).andReturn(userDir);

        EasyMock.replay(info);
    }
}