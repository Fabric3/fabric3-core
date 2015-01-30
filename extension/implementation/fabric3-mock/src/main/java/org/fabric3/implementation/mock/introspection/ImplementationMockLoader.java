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
package org.fabric3.implementation.mock.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.implementation.mock.model.ImplementationMock;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads implementation.mock from a composite. The XML fragment is expected to look like:
 * <pre>
 *  <implementation.mock> org.fabric3.mock.Foo org.fabric3.mock.Bar org.fabric3.mock.Baz </implementation.mock>
 * <pre/>
 * The implementation.mock element is expected to have a delimited list of fully qualified named of the interfaces that need to be mocked.
 */
@EagerInit
public class ImplementationMockLoader implements TypeLoader<ImplementationMock> {
    private static final QName IMPLEMENTATION_MOCK = new QName(org.fabric3.api.Namespaces.F3, "implementation.mock");

    private final MockComponentTypeLoader componentTypeLoader;

    /**
     * Initializes the loader registry.
     *
     * @param componentTypeLoader Component type loader.
     */
    public ImplementationMockLoader(@Reference MockComponentTypeLoader componentTypeLoader) {
        this.componentTypeLoader = componentTypeLoader;
    }

    /**
     * Loads implementation.mock element from a composite.
     *
     * @param reader  StAX reader using which the composite is loaded.
     * @param context Loader context containing contextual information.
     * @return An instance of mock implementation.
     */
    public ImplementationMock load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        assert reader.getName().equals(IMPLEMENTATION_MOCK);

        String textualContent = reader.getElementText().trim();

        List<String> mockedInterfaces = new ArrayList<>();

        StringTokenizer tok = new StringTokenizer(textualContent);
        while (tok.hasMoreElements()) {
            mockedInterfaces.add(tok.nextToken().trim());
        }

        InjectingComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);

        assert reader.getName().equals(IMPLEMENTATION_MOCK);

        return new ImplementationMock(mockedInterfaces, componentType);

    }

}
