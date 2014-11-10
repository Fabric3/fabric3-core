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
package org.fabric3.federation.deployment.coordinator;

import junit.framework.TestCase;

import org.fabric3.federation.deployment.command.DeploymentCommand;

/**
 *
 */
public class DeploymentCacheImplTestCase extends TestCase {

    public void testUndo() throws Exception {
        DeploymentCache cache = new DeploymentCacheImpl();

        DeploymentCommand command1 = new DeploymentCommand("zone1", null, null);
        DeploymentCommand command2 = new DeploymentCommand("zone1", null, null);

        cache.cache(command1);
        assertSame(command1, cache.get());
        cache.cache(command2);
        assertSame(command2, cache.get());
        cache.undo();
        assertSame(command1, cache.get());
        cache.undo();
        assertNull(cache.get());
    }

    public void testThreshold() throws Exception {
        DeploymentCache cache = new DeploymentCacheImpl();

        DeploymentCommand command1 = new DeploymentCommand("zone1", null, null);
        DeploymentCommand command2 = new DeploymentCommand("zone1", null, null);
        DeploymentCommand command3 = new DeploymentCommand("zone1", null, null);
        DeploymentCommand command4 = new DeploymentCommand("zone1", null, null);

        cache.cache(command1);
        cache.cache(command2);
        cache.cache(command3);
        cache.cache(command4);

        assertSame(command4, cache.get());
        cache.undo();
        assertSame(command3, cache.get());
        cache.undo();
        assertSame(command2, cache.get());
        cache.undo();
        assertNull(cache.get());

    }

}
