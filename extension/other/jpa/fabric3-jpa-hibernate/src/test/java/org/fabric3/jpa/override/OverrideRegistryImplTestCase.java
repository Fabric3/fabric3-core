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
package org.fabric3.jpa.override;

import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;

import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.spi.contribution.Contribution;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class OverrideRegistryImplTestCase extends TestCase {
    private static final URI CONTRIBUTION_URI = URI.create("test");
    private OverrideRegistryImpl registry = new OverrideRegistryImpl();

    public void testRegisterUnregister() throws Exception {
        PersistenceOverrides overrides = new PersistenceOverrides("unit", Collections.<String, String>emptyMap());
        registry.register(CONTRIBUTION_URI, overrides);
        assertEquals(overrides, registry.resolve("unit"));
        Contribution contribution = new Contribution(CONTRIBUTION_URI);
        registry.onUninstall(contribution);
        assertNull(registry.resolve("unit"));
        registry.register(CONTRIBUTION_URI, overrides);
    }


    public void testDuplicateRegister() throws Exception {
        PersistenceOverrides overrides = new PersistenceOverrides("unit", Collections.<String, String>emptyMap());
        registry.register(CONTRIBUTION_URI, overrides);
        try {
            registry.register(CONTRIBUTION_URI, overrides);
            fail();
        } catch (DuplicateOverridesException e) {
            // expected
        }
    }

}