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
package org.fabric3.wsdl.loader;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
import org.fabric3.spi.contract.MatchResult;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.ElementLoadFailure;
import org.fabric3.spi.introspection.xml.IncompatibleContracts;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.wsdl.contribution.WsdlServiceContractSymbol;
import org.fabric3.wsdl.contribution.impl.PortTypeNotFound;
import org.fabric3.wsdl.model.WsdlServiceContract;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads interface.wsdl elements in a composite.
 */
@EagerInit
public class InterfaceWsdlLoader extends AbstractValidatingTypeLoader<WsdlServiceContract> {
    private MetaDataStore store;
    private ContractMatcher matcher;
    private LoaderHelper helper;

    public InterfaceWsdlLoader(@Reference MetaDataStore store, @Reference ContractMatcher matcher, @Reference LoaderHelper helper) {
        this.store = store;
        this.matcher = matcher;
        this.helper = helper;
        addAttributes("interface", "callbackInterface", "remotable", "requires", "policySets");
    }

    public WsdlServiceContract load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        validateRemotable(reader, startLocation, context);
        WsdlServiceContract wsdlContract = processInterface(reader, startLocation, context);
        processCallbackInterface(reader, wsdlContract, context);
        helper.loadPolicySetsAndIntents(wsdlContract, reader, context);

        validateAttributes(reader, context, wsdlContract);

        LoaderUtil.skipToEndElement(reader);
        return wsdlContract;
    }

    private WsdlServiceContract processInterface(XMLStreamReader reader, Location location, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        String interfaze = reader.getAttributeValue(null, "interface");
        if (interfaze == null) {
            MissingAttribute failure = new MissingAttribute("Interface attribute is required", startLocation);
            context.addError(failure);
            return new WsdlServiceContract(null, null);
        }
        QName portTypeName = parseQName(interfaze, location, context);
        if (portTypeName == null) {
            return new WsdlServiceContract(null, null);
        }
        return resolveContract(portTypeName, reader, context);
    }

    private void processCallbackInterface(XMLStreamReader reader, WsdlServiceContract wsdlContract, IntrospectionContext context) {
        Location startLocation = reader.getLocation();

        String callbackInterfaze = reader.getAttributeValue(null, "callbackInterface");
        if (callbackInterfaze != null) {
            QName callbackName = parseQName(callbackInterfaze, startLocation, context);
            if (callbackName == null) {
                return;
            }
            WsdlServiceContract callbackContract = resolveContract(callbackName, reader, context);
            // validate callback contract specified in WSDL is compatible with the one specified in the interface.wsdl entry
            ServiceContract originalContract = wsdlContract.getCallbackContract();
            if (originalContract != null) {
                MatchResult result = matcher.isAssignableFrom(callbackContract, originalContract, true);
                if (!result.isAssignable()) {
                    IncompatibleContracts error = new IncompatibleContracts("The callback contract specified on interface.wsdl is not compatible with" +
                                                                            " the one specified in the WSDL portType: " + result.getError(),
                                                                            startLocation,
                                                                            callbackContract);
                    context.addError(error);
                }
            }
            wsdlContract.setCallbackContract(callbackContract);
        }
    }

    QName parseQName(String portType, Location location, IntrospectionContext context) {
        try {
            URI uri = new URI(portType);
            String namespace = UriHelper.getDefragmentedNameAsString(uri);
            String localExpression = uri.getFragment();
            if (localExpression == null || !localExpression.toLowerCase().startsWith("wsdl.porttype(") || !localExpression.endsWith(")")) {
                InvalidValue error = new InvalidValue("A port type expression must be specified of the form <namespace>#wsdl.portType(portType): " + portType,
                                                      location);
                context.addError(error);
                return null;
            }
            String localPart = localExpression.substring(14, localExpression.length() - 1);
            return new QName(namespace, localPart);
        } catch (URISyntaxException e) {
            InvalidValue error = new InvalidValue("Invalid port type identifier: " + portType, location, e);
            context.addError(error);
            return null;
        }
    }

    private WsdlServiceContract resolveContract(QName portTypeName, XMLStreamReader reader, IntrospectionContext context) {
        Location startLocation = reader.getLocation();
        WsdlServiceContractSymbol symbol = new WsdlServiceContractSymbol(portTypeName);
        URI contributionUri = context.getContributionUri();
        ResourceElement<WsdlServiceContractSymbol, WsdlServiceContract> element;
        try {
            element = store.resolve(contributionUri, WsdlServiceContract.class, symbol, context);
        } catch (StoreException e) {
            ElementLoadFailure failure = new ElementLoadFailure("Error loading element", e, startLocation);
            context.addError(failure);
            return new WsdlServiceContract(null, null);
        }
        if (element == null) {
            PortTypeNotFound error = new PortTypeNotFound("Port type not found: " + portTypeName);
            context.addError(error);
            return new WsdlServiceContract(null, null);

        }
        WsdlServiceContract contract = element.getValue();
        // return a copy as it may be modified
        return contract.copy();
    }

    private void validateRemotable(XMLStreamReader reader, Location location, IntrospectionContext context) {
        String remotableAttr = reader.getAttributeValue(null, "remotable");
        if (remotableAttr != null) {
            boolean remotable = Boolean.parseBoolean(remotableAttr);
            if (!remotable) {
                InvalidValue error = new InvalidValue("WSDL interfaces cannot set remotable to false", location);
                context.addError(error);
            }
        }
    }

}
