/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.security;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.runtime.HostInfo;

/**
 * @version $Rev: 9419 $ $Date: 2010-09-01 23:56:59 +0200 (Wed, 01 Sep 2010) $
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

        baseDir = new File(getClass().getClassLoader().getResource(KEYSTORE).getFile()).getParentFile().getParentFile();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getBaseDir()).andReturn(baseDir).atLeastOnce();
        EasyMock.replay(info);
        manager = new KeyStoreManagerImpl(info);


    }
}
