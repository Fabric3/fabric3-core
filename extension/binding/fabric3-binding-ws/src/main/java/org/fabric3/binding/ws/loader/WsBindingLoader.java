/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.ws.loader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.spi.binding.handler.BindingHandlerDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class WsBindingLoader implements TypeLoader<WsBindingDefinition> {
    private static final String WSDL_NS = "http://www.w3.org/2004/08/wsdl-instance";
    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("uri", "uri");
        ATTRIBUTES.put("impl", "impl");
        ATTRIBUTES.put("wsdlElement", "wsdlElement");
        ATTRIBUTES.put("wsdlLocation", "wsdlLocation");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("retries", "retries");
    }

    private final LoaderHelper loaderHelper;
    private final LoaderRegistry registry;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     * @param registry     the loader registry
     */
    public WsBindingLoader(@Reference LoaderHelper loaderHelper, @Reference LoaderRegistry registry) {
        this.loaderHelper = loaderHelper;
        this.registry = registry;
    }

    @SuppressWarnings({"unchecked"})
    public WsBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);

        String wsdlElement = reader.getAttributeValue(null, "wsdlElement");
        String wsdlLocation = reader.getAttributeValue(WSDL_NS, "wsdlLocation");
        int retries = parseRetries(reader, context);

        String bindingName = reader.getAttributeValue(null, "name");

        URI targetUri = parseTargetUri(reader, context);

        WsBindingDefinition binding = new WsBindingDefinition(bindingName, targetUri, wsdlLocation, wsdlElement, retries);

        loaderHelper.loadPolicySetsAndIntents(binding, reader, context);

        //Load optional sub elements config parameters
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                try {
                    Object elementValue = registry.load(reader, Object.class, context);
                    if (elementValue instanceof BindingHandlerDefinition) {
                        binding.addHandler((BindingHandlerDefinition) elementValue);
                    } else if (elementValue instanceof Map) {
                        binding.setConfiguration((Map<String, String>) elementValue);
                    }
                } catch (UnrecognizedElementException e) {
                    UnrecognizedElement failure = new UnrecognizedElement(reader);
                    context.addError(failure);
                }

                break;
            case END_ELEMENT:
                String name = reader.getName().getLocalPart();
                if ("binding.ws".equals(name)) {
                    return binding;
                }
                break;
            case XMLStreamConstants.END_DOCUMENT:
                // avoid infinite loop if end element not present
                return binding;
            }
        }
    }

    private URI parseTargetUri(XMLStreamReader reader, IntrospectionContext context) {
        String uri = reader.getAttributeValue(null, "uri");
        URI targetUri = null;
        if (uri != null) {
            try {
                targetUri = new URI(uri);
            } catch (URISyntaxException ex) {
                InvalidValue failure = new InvalidValue("The web services binding URI is not a valid: " + uri, reader);
                context.addError(failure);
            }
        }
        return targetUri;
    }

    private int parseRetries(XMLStreamReader reader, IntrospectionContext context) {
        String retries = reader.getAttributeValue(null, "retries");
        if (retries != null) {
            try {
                return Integer.parseInt(retries);
            } catch (NumberFormatException e) {
                InvalidValue error = new InvalidValue("The retries attribute must be a valid number", reader);
                context.addError(error);
            }
        }
        return 0;
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

}
