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
package org.fabric3.contribution.scanner.scanner.impl;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.contribution.scanner.impl.ContributionTrackerImpl;
import org.fabric3.contribution.scanner.impl.ContributionTrackerMonitor;
import org.fabric3.api.host.runtime.HostInfo;

/**
 *
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
