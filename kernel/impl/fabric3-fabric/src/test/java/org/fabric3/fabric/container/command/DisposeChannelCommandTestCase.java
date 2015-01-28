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

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.spi.model.physical.PhysicalChannelDefinition;

public class DisposeChannelCommandTestCase extends TestCase {
    private PhysicalChannelDefinition definition;

    public void testEquals() throws Exception {
        DisposeChannelCommand command1 = new DisposeChannelCommand(definition);
        DisposeChannelCommand command2 = new DisposeChannelCommand(definition);
        assertEquals(command1, command2);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        URI uri = URI.create("channel");
        QName deployable = new QName("test", "composite");
        definition = new PhysicalChannelDefinition(uri, deployable);
    }
}
