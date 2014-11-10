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
package org.fabric3.fabric.security;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.host.runtime.HostInfo;

/**
 *
 */
public class KeyStoreManagerImplTestCase extends TestCase {
    private static final String KEYSTORE = "org/fabric3/fabric/security/config/fabric3-keystore.jks";
    private KeyStoreManagerImpl manager;
    private File baseDir;

    public void testInitializeKeyStoreUsingStandardLocation() throws Exception {
        manager.setKeyStorePassword("password");
        manager.init();
        assertEquals("password", manager.getKeyStorePassword());
        assertEquals("password", manager.getTrustStorePassword());
        assertTrue(manager.getKeyStoreLocation().exists());
        assertTrue(manager.getTrustStoreLocation().exists());
        assertNotNull(manager.getKeyStore());
        assertNotNull(manager.getTrustStore());
    }

    public void testInitializeKeyStoreUsingConfiguredLocation() throws Exception {
        manager.setKeyStorePassword("password");
        File config = new File(baseDir, "config");
        String keystore = new File(config, "fabric3-keystore.jks").toString();
        manager.setKeyStoreLocation(keystore);
        manager.init();
        assertNotNull(manager.getKeyStore());
        assertNotNull(manager.getTrustStore());
    }

    public void testInitializeTrustStoreUsingConfiguredLocation() throws Exception {
        manager.setKeyStorePassword("password");
        manager.setTrustStorePassword("password");
        File config = new File(baseDir, "config");
        String location = new File(config, "fabric3-keystore.jks").toString();
        manager.setKeyStoreLocation(location);
        manager.setTrustStoreLocation(location);
        manager.init();
        assertNotNull(manager.getKeyStore());
        assertNotNull(manager.getTrustStore());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.clearProperty("javax.net.ssl.keyStore");
        baseDir = new File(getClass().getClassLoader().getResource(KEYSTORE).getFile()).getParentFile().getParentFile();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getBaseDir()).andReturn(baseDir).atLeastOnce();
        EasyMock.replay(info);
        manager = new KeyStoreManagerImpl(info);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.clearProperty("javax.net.ssl.keyStore");
    }
}
