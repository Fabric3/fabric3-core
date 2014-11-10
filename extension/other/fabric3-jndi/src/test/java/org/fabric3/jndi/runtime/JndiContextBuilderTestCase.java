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
package org.fabric3.jndi.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.jndi.provision.PhysicalJndiContextDefinition;
import org.fabric3.jndi.spi.JndiContextManager;

/**
 *
 */
public class JndiContextBuilderTestCase extends TestCase {
    private JndiContextBuilder builder;
    private JndiContextManager manager;
    private PhysicalJndiContextDefinition definition;

    public void testBuild() throws Exception {
        manager.register(EasyMock.eq("context1"), EasyMock.isA(Properties.class));
        EasyMock.replay(manager);

        builder.build(definition);

        EasyMock.verify(manager);
    }

    public void testRemove() throws Exception {
        manager.unregister("context1");
        EasyMock.replay(manager);

        builder.remove(definition);

        EasyMock.verify(manager);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        manager = EasyMock.createMock(JndiContextManager.class);
        builder = new JndiContextBuilder(manager);

        Map<String, Properties> contexts = new HashMap<>();
        contexts.put("context1", new Properties());
        definition = new PhysicalJndiContextDefinition(contexts);
    }

}