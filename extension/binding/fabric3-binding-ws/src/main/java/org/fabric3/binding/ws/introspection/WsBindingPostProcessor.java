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
package org.fabric3.binding.ws.introspection;

import java.lang.reflect.AccessibleObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.binding.ws.annotation.BindingConfiguration;
import org.fabric3.api.binding.ws.annotation.WebServiceBinding;
import org.fabric3.api.binding.ws.model.WsBinding;
import org.fabric3.api.model.type.component.Binding;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.api.model.type.java.InjectingComponentType;
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

    protected Binding processService(WebServiceBinding annotation,
                                               Service<ComponentType> service,
                                               InjectingComponentType componentType,
                                               Class<?> implClass,
                                               IntrospectionContext context) {
        return createDefinition(annotation, implClass, context);

    }

    protected Binding processServiceCallback(WebServiceBinding annotation,
                                                       Service<ComponentType> service,
                                                       InjectingComponentType componentType,
                                                       Class<?> implClass,
                                                       IntrospectionContext context) {
        return null; // not yet supported
    }

    protected Binding processReference(WebServiceBinding annotation,
                                                 Reference reference,
                                                 AccessibleObject object,
                                                 Class<?> implClass,
                                                 IntrospectionContext context) {
        return createDefinition(annotation, implClass, context);
    }

    protected Binding processReferenceCallback(WebServiceBinding annotation,
                                                         Reference reference,
                                                         AccessibleObject object,
                                                         Class<?> implClass,
                                                         IntrospectionContext context) {
        return null; // not yet supported
    }

    private WsBinding createDefinition(WebServiceBinding annotation, Class<?> implClass, IntrospectionContext context) {
        String name = annotation.name();
        if (name.isEmpty()) {
            name = "WSBinding";
        }
        URI uri = parseUri(annotation, implClass, context);
        String wsdlLocation = getNullibleValue(annotation.wsdlLocation());
        String wsdlElement = getNullibleValue(annotation.wsdlElement());
        int retries = annotation.retries();

        WsBinding binding = new WsBinding(name, uri, wsdlLocation, wsdlElement, retries);

        parseConfiguration(annotation, binding);
        return binding;
    }

    private void parseConfiguration(WebServiceBinding annotation, WsBinding binding) {
        BindingConfiguration[] configurations = annotation.configuration();
        if (configurations.length == 0) {
            return;
        }
        Map<String, String> map = new HashMap<>();
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

