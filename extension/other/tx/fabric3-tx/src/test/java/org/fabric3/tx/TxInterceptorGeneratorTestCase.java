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
package org.fabric3.tx;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.w3c.dom.Element;

/**
 *
 */
public class TxInterceptorGeneratorTestCase extends TestCase {

    public void testGenerate() throws Exception {
        Element element = EasyMock.createMock(Element.class);
        EasyMock.expect(element.getAttribute("action")).andReturn("BEGIN");
        EasyMock.replay(element);

        TxInterceptorGenerator generator = new TxInterceptorGenerator();
        TxInterceptorDefinition definition = generator.generate(element, null, null);
        assertEquals(TxAction.BEGIN, definition.getAction());
        EasyMock.verify(element);
    }
}
