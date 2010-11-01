/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.contribution.scanner.scanner.impl;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.scanner.impl.ContributionTrackerImpl;
import org.fabric3.contribution.scanner.impl.ContributionTrackerMonitor;
import org.fabric3.host.runtime.HostInfo;

/**
 * @version $Rev: 9626 $ $Date: 2010-11-01 00:37:41 +0100 (Mon, 01 Nov 2010) $
 */
public class ContributionTrackerImplTestCase extends TestCase {
    private HostInfo info;
    private MockXMLFactory factory;
    private ContributionTrackerMonitor monitor;

    public void testWriteRead() throws Exception {
        ContributionTrackerImpl tracker = new ContributionTrackerImpl(factory, info, monitor);
        tracker.init();

        tracker.addResource("resource1");
        tracker.addResource("resource2");

        ContributionTrackerImpl tracker2 = new ContributionTrackerImpl(factory, info, monitor);
        tracker2.init();

        assertTrue(tracker2.isTracked("resource1"));
        assertTrue(tracker2.isTracked("resource2"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clean();
        File file = new File(".");
        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDataDir()).andReturn(file).atLeastOnce();
        EasyMock.replay(info);
        factory = new MockXMLFactory();
        monitor = EasyMock.createMock(ContributionTrackerMonitor.class);
        EasyMock.replay(monitor);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clean();
    }

    private void clean() {
        File contributions = new File(".", "contributions.xml");
        if (contributions.exists()) {
            contributions.delete();
        }
    }

}
