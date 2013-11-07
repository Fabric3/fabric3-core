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
package org.fabric3.binding.ws.introspection;

import java.lang.reflect.AccessibleObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.binding.ws.annotation.BindingConfiguration;
import org.fabric3.api.binding.ws.annotation.WebServiceBinding;
import org.fabric3.api.binding.ws.model.WsBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.AbstractBindingPostProcessor;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Introspects WS binding information in a component implementation.
 */
@EagerInit
public class WsBindingPostProcessor extends AbstractBindingPostProcessor<WebServiceBinding> {

    public WsBindingPostProcessor() {
        super(WebServiceBinding.class);
    }

    protected BindingDefinition processService(WebServiceBinding annotation,
                                               AbstractService<?> service,
                                               InjectingComponentType componentType,
                                               Class<?> implClass,
                                               IntrospectionContext context) {
        return createDefinition(annotation, implClass, context);

    }

    protected BindingDefinition processServiceCallback(WebServiceBinding annotation,
                                                       AbstractService<?> service,
                                                       InjectingComponentType componentType,
                                                       Class<?> implClass,
                                                       IntrospectionContext context) {
        return null; // not yet supported
    }

    protected BindingDefinition processReference(WebServiceBinding annotation,
                                                 ReferenceDefinition reference,
                                                 AccessibleObject object,
                                                 Class<?> implClass,
                                                 IntrospectionContext context) {
        return createDefinition(annotation, implClass, context);
    }

    protected BindingDefinition processReferenceCallback(WebServiceBinding annotation,
                                                         ReferenceDefinition reference,
                                                         AccessibleObject object,
                                                         Class<?> implClass,
                                                         IntrospectionContext context) {
        return null; // not yet supported
    }

    private WsBindingDefinition createDefinition(WebServiceBinding annotation, Class<?> implClass, IntrospectionContext context) {
        String name = annotation.name();
        if (name.isEmpty()) {
            name = "WSBinding";
        }
        URI uri = parseUri(annotation, implClass, context);
        String wsdlLocation = getNullibleValue(annotation.wsdlLocation());
        String wsdlElement = getNullibleValue(annotation.wsdlElement());
        int retries = annotation.retries();

        WsBindingDefinition binding = new WsBindingDefinition(name, uri, wsdlLocation, wsdlElement, retries);

        parseConfiguration(annotation, binding);
        return binding;
    }

    private void parseConfiguration(WebServiceBinding annotation, WsBindingDefinition binding) {
        BindingConfiguration[] configurations = annotation.configuration();
        if (configurations.length == 0) {
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        for (BindingConfiguration configuration : configurations) {
            map.put(configuration.key(), configuration.value());
        }
        binding.setConfiguration(map);
    }

    private URI parseUri(WebServiceBinding annotation, Class<?> implClass, IntrospectionContext context) {
        String uriString = getNullibleValue(annotation.uri());
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            InvalidAnnotation error = new InvalidAnnotation("Invalid web service binding uri", implClass, annotation, implClass, e);
            context.addError(error);
        }
        return URI.create("errorUri");
    }

}

