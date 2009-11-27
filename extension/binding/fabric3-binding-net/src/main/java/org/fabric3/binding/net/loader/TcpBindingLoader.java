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
package org.fabric3.binding.net.loader;

import java.net.URI;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.net.config.TcpConfig;
import org.fabric3.binding.net.model.TcpBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * Loader for binding.http.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class TcpBindingLoader extends AbstractBindingLoader<TcpBindingDefinition> {
    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public TcpBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public TcpBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        URI uri = parseUri(reader, context);
        String scheme = uri.getScheme();
        if (scheme != null && !"tcp".equalsIgnoreCase(scheme)) {
            InvalidValue failure = new InvalidValue("Absolute binding URIs must use TCP as the scheme", reader);
            context.addError(failure);
        }
        TcpBindingDefinition definition = new TcpBindingDefinition(uri);
        TcpConfig config = definition.getConfig();
        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);
        parseBindingAttributes(reader, config, context);
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.END_ELEMENT:
                if ("binding.tcp".equals(reader.getName().getLocalPart())) {
                    String wireFormat = definition.getConfig().getWireFormat();
                    if (wireFormat != null && !"jdk".equals(wireFormat)) {
                        // record the wire format requirement so the extension can be provisioned
                        definition.addRequiredCapability(wireFormat);
                    }
                    return definition;
                }
                break;
            case XMLStreamConstants.START_ELEMENT:
                String name = reader.getName().getLocalPart();
                if (name.startsWith("wireFormat.")) {
                    parseWireFormat(reader, config, false, context);
                } else if ("response".equals(name)) {
                    parseResponse(reader, config, context);
                } else if ("sslSettings".equals(name)) {
                    parseSslSettings(reader, config, context);
                }
                break;
            }

        }
    }

}