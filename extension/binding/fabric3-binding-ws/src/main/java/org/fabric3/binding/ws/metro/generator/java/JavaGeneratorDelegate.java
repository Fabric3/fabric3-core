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

package org.fabric3.binding.ws.metro.generator.java;

import javax.jws.WebService;
import javax.xml.parsers.ParserConfigurationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.ws.model.WsBinding;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.binding.ws.metro.generator.GenerationHelper;
import org.fabric3.binding.ws.metro.generator.MetroGeneratorDelegate;
import org.fabric3.binding.ws.metro.generator.java.codegen.GeneratedInterface;
import org.fabric3.binding.ws.metro.generator.java.codegen.InterfaceGenerator;
import org.fabric3.binding.ws.metro.generator.resolver.TargetUrlResolver;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroJavaWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroJavaWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.util.ClassLoaderUpdater;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;

/**
 * Generates source and target definitions for an endpoint defined by a Java-based service contract.
 */
@Key("org.fabric3.spi.model.type.java.JavaServiceContract")
public class JavaGeneratorDelegate implements MetroGeneratorDelegate<JavaServiceContract> {

    private EndpointSynthesizer synthesizer;
    private InterfaceGenerator interfaceGenerator;
    private ClassLoaderUpdater classLoaderUpdater;
    private TargetUrlResolver targetUrlResolver;
    private HostInfo info;

    public JavaGeneratorDelegate(@Reference EndpointSynthesizer synthesizer,
                                 @Reference InterfaceGenerator interfaceGenerator,
                                 @Reference ClassLoaderUpdater classLoaderUpdater,
                                 @Reference TargetUrlResolver targetUrlResolver,
                                 @Reference HostInfo info) throws ParserConfigurationException {
        this.synthesizer = synthesizer;
        this.interfaceGenerator = interfaceGenerator;
        this.classLoaderUpdater = classLoaderUpdater;
        this.targetUrlResolver = targetUrlResolver;
        this.info = info;
    }

    public MetroJavaWireSourceDefinition generateSource(LogicalBinding<WsBinding> binding, JavaServiceContract contract) {

        Class<?> serviceClass = contract.getInterfaceClass();
        WsBinding bindingDefinition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(bindingDefinition, serviceClass);

        ServiceEndpointDefinition endpointDefinition = createServiceEndpointDefinition(binding, contract, serviceClass);

        Class<?> interfaze = contract.getInterfaceClass();

        // create handler definitions
        List<PhysicalBindingHandlerDefinition> handlers = GenerationHelper.generateBindingHandlers(info.getDomain(), bindingDefinition);

        String wsdl = null;
        Map<String, String> schemas = Collections.emptyMap();

        // update the classloader
        classLoaderUpdater.updateClassLoader(serviceClass);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(serviceClass.getClassLoader());

            if (interfaceGenerator.doGeneration(serviceClass)) {
                // if the service interface is not annotated, generate an implementing class that is
                GeneratedInterface generatedInterface = interfaceGenerator.generate(serviceClass, null, null, null, null);
                serviceClass = generatedInterface.getGeneratedClass();
                interfaze = serviceClass;
            }
            if (endpointDefinition.getWsdl() != null) {
                // No policy specified, use the WSDL specified via wsdlElement or wsdlLocation. If one is not specified, wsdl will be generated from
                // introspecting the Java provider class. If the WSDL is specified, it will be used instead when the endpoint is created.
                wsdl = endpointDefinition.getWsdl();
            }
            URI serviceUri = null;
            if (binding.isCallback()) {
                LogicalComponent<?> component = binding.getParent().getParent();
                for (LogicalService service : component.getServices()) {
                    if (service.getServiceContract().getQualifiedInterfaceName().equals(contract.getQualifiedInterfaceName())) {
                        try {
                            serviceUri = new URI(component.getUri() + "#" + service.getDefinition().getName());
                        } catch (URISyntaxException e) {
                            throw new Fabric3Exception(e);
                        }
                        break;
                    }
                }
            } else {
                serviceUri = binding.getParent().getUri();
            }

            boolean bidirectional = contract.getCallbackContract() != null && !binding.isCallback();

            return new MetroJavaWireSourceDefinition(serviceUri, endpointDefinition, interfaze, wsdl, schemas, wsdlLocation, bidirectional, handlers);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        //        }
    }

    public MetroWireTargetDefinition generateTarget(LogicalBinding<WsBinding> binding, JavaServiceContract contract) {
        URL targetUrl = null;
        WsBinding bindingDefinition = binding.getDefinition();
        URI targetUri = bindingDefinition.getTargetUri();

        if (binding.isCallback() && targetUri != null) {
            throw new Fabric3Exception("A web services callback binding cannot be used with a binding URI on a service: " + binding.getParent().getUri());
        }

        if (targetUri != null) {
            if (!targetUri.isAbsolute() && !binding.isCallback()) {
                throw new Fabric3Exception("Web service binding URI must be absolute on reference: " + binding.getParent().getUri());
            }
            try {
                targetUrl = targetUri.toURL();
            } catch (MalformedURLException e) {
                throw new Fabric3Exception(e);
            }
        } else if (bindingDefinition.getWsdlElement() == null && bindingDefinition.getWsdlLocation() == null && !binding.isCallback()) {
            throw new Fabric3Exception("A web service binding URI must be specified: " + binding.getParent().getUri());
        }

        return generateTarget(binding, targetUrl, contract);
    }

    public MetroWireTargetDefinition generateServiceBindingTarget(LogicalBinding<WsBinding> serviceBinding, JavaServiceContract contract) {
        URL targetUrl = targetUrlResolver.resolveUrl(serviceBinding);
        return generateTarget(serviceBinding, targetUrl, contract);
    }

    private MetroWireTargetDefinition generateTarget(LogicalBinding<WsBinding> binding, URL targetUrl, JavaServiceContract contract) {
        Class<?> serviceClass = contract.getInterfaceClass();
        WsBinding bindingDefinition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(bindingDefinition, serviceClass);

        ReferenceEndpointDefinition endpointDefinition = createReferenceEndpointDefinition(binding, contract, serviceClass, targetUrl);

        Class<?> interfaze = contract.getInterfaceClass();

        List<PhysicalBindingHandlerDefinition> handlers = GenerationHelper.generateBindingHandlers(info.getDomain(), bindingDefinition);

        String wsdl = null;
        Map<String, String> schemas = Collections.emptyMap();

        // update the classloader
        classLoaderUpdater.updateClassLoader(serviceClass);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(serviceClass.getClassLoader());

            if (interfaceGenerator.doGeneration(serviceClass)) {
                // if the service interface is not annotated, generate an implementing class that is
                GeneratedInterface generatedInterface = interfaceGenerator.generate(serviceClass, null, null, null, null);
                serviceClass = generatedInterface.getGeneratedClass();
                interfaze = serviceClass;
            }
            if (endpointDefinition.getWsdl() != null) {
                // Wsdl will be null and
                // one will be downloaded from the endpoint address (?wsdl) when the reference proxy is created on a runtime and cached. If the
                // WSDL is specified, it will be used instead when the reference proxy is created.
                wsdl = endpointDefinition.getWsdl();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        // obtain connection information
        ConnectionConfiguration connectionConfiguration = GenerationHelper.createConnectionConfiguration(bindingDefinition);

        boolean bidirectional = contract.getCallbackContract() != null && !binding.isCallback();

        int retries = bindingDefinition.getRetries();
        MetroJavaWireTargetDefinition targetDefinition = new MetroJavaWireTargetDefinition(endpointDefinition,
                                                                                           interfaze,
                                                                                           wsdl,
                                                                                           schemas,
                                                                                           wsdlLocation,
                                                                                           connectionConfiguration,
                                                                                           retries,
                                                                                           bidirectional,
                                                                                           handlers);
        if (binding.isCallback()) {
            targetDefinition.setUri(binding.getParent().getUri());
        }
        return targetDefinition;
    }

    /**
     * Returns the WSDL location if one is defined in the binding configuration or null.
     *
     * @param definition   the binding configuration
     * @param serviceClass the service endpoint interface
     * @return the WSDL location or null
     * @ if the WSDL location is invalid
     */
    private URL getWsdlLocation(WsBinding definition, Class<?> serviceClass) {
        try {
            String location = definition.getWsdlLocation();
            if (location != null) {
                return new URL(location);
            }
            WebService annotation = serviceClass.getAnnotation(WebService.class);
            if (annotation != null) {
                String wsdlLocation = annotation.wsdlLocation();
                if (wsdlLocation.length() > 0) {
                    return new URL(wsdlLocation);
                } else {
                    return null;
                }
            }
        } catch (MalformedURLException e) {
            throw new Fabric3Exception(e);
        }
        return null;

    }

    private ServiceEndpointDefinition createServiceEndpointDefinition(LogicalBinding<WsBinding> binding, JavaServiceContract contract, Class<?> serviceClass) {
        URI targetUri = binding.getDefinition().getTargetUri();
        if (targetUri == null) {
            targetUri = URI.create(binding.getParent().getUri().getFragment());  // use the service URI fragment
        }
        return synthesizer.synthesizeServiceEndpoint(contract, serviceClass, targetUri);
    }

    private ReferenceEndpointDefinition createReferenceEndpointDefinition(LogicalBinding<WsBinding> binding,
                                                                          JavaServiceContract contract,
                                                                          Class<?> serviceClass,
                                                                          URL targetUrl) {
        if (binding.isCallback()) {
            targetUrl = ReferenceEndpointDefinition.DYNAMIC_URL;
        }

        if (targetUrl != null) {
            return synthesizer.synthesizeReferenceEndpoint(contract, serviceClass, targetUrl);
        } else {
            throw new Fabric3Exception("Target URL must be specified");
        }
    }

}
