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
package org.fabric3.introspection.xml.writer;

import javax.xml.stream.XMLStreamWriter;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.api.model.type.component.ComponentDefinition;
import org.fabric3.spi.introspection.xml.TypeWriter;
import org.fabric3.spi.introspection.xml.UnrecognizedTypeException;
import org.fabric3.spi.introspection.xml.Writer;

/**
 *
 */
public class WriterImplTestCase extends TestCase {
    private Writer writer = new WriterImpl();

    @SuppressWarnings({"unchecked"})
    public void testRegisterDispatchDeregister() throws Exception {
        TypeWriter<ComponentDefinition> typeWriter = EasyMock.createMock(TypeWriter.class);
        typeWriter.write(EasyMock.isA(ComponentDefinition.class), EasyMock.isA(XMLStreamWriter.class));
        EasyMock.expectLastCall();
        EasyMock.replay(typeWriter);

        writer.register(ComponentDefinition.class, typeWriter);
        XMLStreamWriter xmlWriter = EasyMock.createMock(XMLStreamWriter.class);
        writer.write(new ComponentDefinition(null), xmlWriter);
        writer.unregister(ComponentDefinition.class);
        try {
            writer.write(new ComponentDefinition(null), xmlWriter);
            fail();
        } catch (UnrecognizedTypeException e) {
            // expected
        }
        EasyMock.verify(typeWriter);
    }
}
