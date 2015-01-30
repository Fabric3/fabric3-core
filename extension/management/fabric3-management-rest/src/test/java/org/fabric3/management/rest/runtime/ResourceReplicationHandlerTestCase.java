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
 */
package org.fabric3.management.rest.runtime;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.management.rest.spi.ResourceHost;
import org.fabric3.management.rest.spi.Verb;

/**
 *
 */
public class ResourceReplicationHandlerTestCase extends TestCase {

    public void testOnMessage() throws Exception {
        String path = "/service";
        Object[] params = {"test"};

        ResourceHost host = EasyMock.createMock(ResourceHost.class);
        host.dispatch("/service", Verb.POST, params);
        ManagementMonitor monitor = EasyMock.createNiceMock(ManagementMonitor.class);

        EasyMock.replay(host, monitor);

        ResourceReplicationHandler handler = new ResourceReplicationHandler(host, monitor);

        ReplicationEnvelope envelope = new ReplicationEnvelope(path, Verb.POST, params);
        handler.onMessage(envelope);

        EasyMock.verify(host, monitor);
    }
}
