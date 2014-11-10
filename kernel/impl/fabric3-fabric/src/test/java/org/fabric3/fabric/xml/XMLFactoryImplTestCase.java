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
package org.fabric3.fabric.xml;

import junit.framework.TestCase;

/**
 *
 */
public final class XMLFactoryImplTestCase extends TestCase {
    private XMLFactoryImpl factory;

    public void testNewInputFactoryInstance() throws Exception {
        assertNotNull(factory.newInputFactoryInstance());
    }

    public void testNewOutputFactoryInstance() throws Exception {
        assertNotNull(factory.newOutputFactoryInstance());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        factory = new XMLFactoryImpl();
    }
}