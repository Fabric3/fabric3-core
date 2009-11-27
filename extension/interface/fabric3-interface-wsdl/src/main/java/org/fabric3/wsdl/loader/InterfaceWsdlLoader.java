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
package org.fabric3.wsdl.loader;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.StoreException;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.wsdl.contribution.WsdlServiceContractSymbol;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * Loads interface.wsdl elements in a composite.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class InterfaceWsdlLoader implements TypeLoader<WsdlServiceContract> {
    private MetaDataStore store;

    public InterfaceWsdlLoader(@Reference MetaDataStore store) {
        this.store = store;
    }

    public WsdlServiceContract load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        WsdlServiceContract wsdlContract = processInterface(reader, context);
        processCallbackInterface(reader, wsdlContract, context);
        LoaderUtil.skipToEndElement(reader);
        return wsdlContract;
    }

    private WsdlServiceContract processInterface(XMLStreamReader reader, IntrospectionContext context) {
        String interfaze = reader.getAttributeValue(null, "interface");
        if (interfaze == null) {
            MissingAttribute failure = new MissingAttribute("Interface attribute is required", reader);
            context.addError(failure);
            return null;
        }
        QName portTypeName = parseQName(interfaze, reader, context);
        if (portTypeName == null) {
            return null;
        }
        return resolveContract(portTypeName, reader, context);
    }

    private void processCallbackInterface(XMLStreamReader reader, WsdlServiceContract wsdlContract, IntrospectionContext context) {
        String callbackInterfaze = reader.getAttributeValue(null, "callbackInterface");
        if (callbackInterfaze != null) {
            QName callbackName = parseQName(callbackInterfaze, reader, context);
            if (callbackName == null) {
                return;
            }
            WsdlServiceContract callbackContract = resolveContract(callbackName, reader, context);
            wsdlContract.setCallbackContract(callbackContract);
        }
    }

    QName parseQName(String portType, XMLStreamReader reader, IntrospectionContext context) {
        try {
            URI uri = new URI(portType);
            String namespace = UriHelper.getDefragmentedNameAsString(uri);
            String localExpression = uri.getFragment();
            if (localExpression == null || !localExpression.startsWith("wsdl11.portType(") || !localExpression.endsWith(")")) {
                InvalidValue error = new InvalidValue("A port type expression must be specified of the form: <namespace>#wsdl11.portType(portType)"
                        + portType, reader);
                context.addError(error);
                return null;
            }
            String localPart = localExpression.substring(16, localExpression.length() - 1);
            return new QName(namespace, localPart);
        } catch (URISyntaxException e) {
            InvalidValue error = new InvalidValue("Invalid port type identifier: " + portType, reader, e);
            context.addError(error);
            return null;
        }
    }

    private WsdlServiceContract resolveContract(QName portTypeName, XMLStreamReader reader, IntrospectionContext context) {
        WsdlServiceContractSymbol symbol = new WsdlServiceContractSymbol(portTypeName);
        URI contributionUri = context.getContributionUri();
        ResourceElement<WsdlServiceContractSymbol, WsdlServiceContract> element;
        try {
            element = store.resolve(contributionUri, WsdlServiceContract.class, symbol, context);
        } catch (StoreException e) {
            ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, reader);
            context.addError(failure);
            return null;
        }
        if (element == null) {
            PortTypeNotFound error = new PortTypeNotFound("Port type not found: " + portTypeName);
            context.addError(error);
            return null;
        }
        return element.getValue();
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"interface".equals(name) && !"callbackInterface".equals(name) && !"remotable".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
