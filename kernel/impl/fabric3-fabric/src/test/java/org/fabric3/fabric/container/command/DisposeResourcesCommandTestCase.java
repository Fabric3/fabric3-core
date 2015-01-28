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
package org.fabric3.fabric.container.command;

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.spi.model.physical.PhysicalResourceDefinition;

public class DisposeResourcesCommandTestCase extends TestCase {
    private DisposeResourcesCommand command;
    private List<PhysicalResourceDefinition> definitions;

    public void testEquals() throws Exception {
        DisposeResourcesCommand command2 = new DisposeResourcesCommand(definitions);
        assertEquals(command2, command);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        definitions = Collections.<PhysicalResourceDefinition>singletonList(new MockDefinition());
        command = new DisposeResourcesCommand(definitions);
    }

    private class MockDefinition extends PhysicalResourceDefinition {
        private static final long serialVersionUID = 6358317322714807832L;
    }
}
