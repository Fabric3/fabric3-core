/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.implementation.mock.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.fabric3.implementation.mock.model.ImplementationMock;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.model.type.java.InjectingComponentType;
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

        assert reader.getName().equals(ImplementationMock.IMPLEMENTATION_MOCK);

        String textualContent = reader.getElementText().trim();

        List<String> mockedInterfaces = new ArrayList<String>();

        StringTokenizer tok = new StringTokenizer(textualContent);
        while (tok.hasMoreElements()) {
            mockedInterfaces.add(tok.nextToken().trim());
        }

        InjectingComponentType componentType = componentTypeLoader.load(mockedInterfaces, context);

        assert reader.getName().equals(ImplementationMock.IMPLEMENTATION_MOCK);

        return new ImplementationMock(mockedInterfaces, componentType);

    }

}
