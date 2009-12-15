/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.test;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.host.Namespaces;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * Parses <code>binding.test</code> for services and references. A uri to bind the service to or target a reference must be provided as an attribute.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TestBindingLoader implements TypeLoader<TestBindingDefinition> {

    public static final QName BINDING_QNAME = new QName(Namespaces.BINDING, "binding.test");

    public TestBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        TestBindingDefinition definition = null;
        String uri = null;
        try {
            uri = reader.getAttributeValue(null, "uri");
            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("The uri attribute is not specified", reader);
                context.addError(failure);
                return null;
            }
            URI targetUri = new URI(uri);
            String name = reader.getAttributeValue(null, "name");
            definition = new TestBindingDefinition(name, targetUri);
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The binding URI is not valid: " + uri, reader);
            context.addError(failure);
        }
        LoaderUtil.skipToEndElement(reader);
        return definition;

    }

}
