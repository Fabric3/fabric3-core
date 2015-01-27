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
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.xml.ws.api.BindingID;
import org.fabric3.api.binding.ws.model.WsBindingDefinition;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.binding.ws.metro.generator.GenerationHelper;
import org.fabric3.binding.ws.metro.generator.MetroGeneratorDelegate;
import org.fabric3.binding.ws.metro.generator.PolicyExpressionMapping;
import org.fabric3.binding.ws.metro.generator.java.codegen.GeneratedInterface;
import org.fabric3.binding.ws.metro.generator.java.codegen.InterfaceGenerator;
import org.fabric3.binding.ws.metro.generator.java.wsdl.GeneratedArtifacts;
import org.fabric3.binding.ws.metro.generator.java.wsdl.JavaWsdlGenerator;
import org.fabric3.binding.ws.metro.generator.policy.WsdlPolicyAttacher;
import org.fabric3.binding.ws.metro.generator.resolver.TargetUrlResolver;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroJavaWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroJavaWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.util.BindingIdResolver;
import org.fabric3.binding.ws.metro.util.ClassLoaderUpdater;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Generates source and target definitions for an endpoint defined by a Java-based service contract.
 */
public class JavaGeneratorDelegate implements MetroGeneratorDelegate<JavaServiceContract> {
    private static final String REPLACEABLE_ADDRESS = "REPLACE_WITH_ACTUAL_URL";

    private EndpointSynthesizer synthesizer;
    private JavaWsdlGenerator wsdlGenerator;
    private InterfaceGenerator interfaceGenerator;
    private BindingIdResolver bindingIdResolver;
    private WsdlPolicyAttacher policyAttacher;
    private ClassLoaderRegistry classLoaderRegistry;
    private ClassLoaderUpdater classLoaderUpdater;
    private TargetUrlResolver targetUrlResolver;
    private HostInfo info;
    private DocumentBuilder documentBuilder;
    private TransformerFactory transformerFactory;

    public JavaGeneratorDelegate(@Reference EndpointSynthesizer synthesizer,
                                 @Reference JavaWsdlGenerator wsdlGenerator,
                                 @Reference InterfaceGenerator interfaceGenerator,
                                 @Reference BindingIdResolver bindingIdResolver,
                                 @Reference WsdlPolicyAttacher policyAttacher,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference ClassLoaderUpdater classLoaderUpdater,
                                 @Reference TargetUrlResolver targetUrlResolver,
                                 @Reference HostInfo info) throws ParserConfigurationException {
        this.synthesizer = synthesizer;
        this.wsdlGenerator = wsdlGenerator;
        this.interfaceGenerator = interfaceGenerator;
        this.bindingIdResolver = bindingIdResolver;
        this.policyAttacher = policyAttacher;
        this.classLoaderRegistry = classLoaderRegistry;
        this.classLoaderUpdater = classLoaderUpdater;
        this.targetUrlResolver = targetUrlResolver;
        this.info = info;
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        transformerFactory = TransformerFactory.newInstance();
    }

    public MetroJavaWireSourceDefinition generateSource(LogicalBinding<WsBindingDefinition> binding, JavaServiceContract contract, EffectivePolicy policy)
            throws GenerationException {

        URI contributionUri = binding.getParent().getParent().getDefinition().getContributionUri();
        Class<?> serviceClass = loadServiceClass(contract, contributionUri);
        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition, serviceClass);

        ServiceEndpointDefinition endpointDefinition = createServiceEndpointDefinition(binding, contract, serviceClass);

        String interfaze = contract.getQualifiedInterfaceName();

        // handle endpoint-level intents provided by Metro
        List<QName> intentNames = new ArrayList<>();
        Set<Intent> endpointIntents = policy.getProvidedEndpointIntents();
        for (Intent intent : endpointIntents) {
            intentNames.add(intent.getName());
        }

        // handle endpoint-level policies
        List<Element> policyExpressions = new ArrayList<>();
        for (PolicySet policySet : policy.getEndpointPolicySets()) {
            Element expression = policySet.getExpression();
            if (expression == null) {
                // empty policy set
                continue;
            }
            policyExpressions.add(expression);
        }

        // Note operation level provided intents are not currently supported. Intents are mapped to JAX-WS features, which are per endpoint.
        List<PolicyExpressionMapping> mappings = GenerationHelper.createMappings(policy, serviceClass);

        // create handler definitions
        List<PhysicalBindingHandlerDefinition> handlers = GenerationHelper.generateBindingHandlers(info.getDomain(), definition);

        byte[] generatedBytes = null;
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
                generatedBytes = generatedInterface.getBytes();
                serviceClass = generatedInterface.getGeneratedClass();
                interfaze = serviceClass.getName();
            }
            if (!policyExpressions.isEmpty() || !mappings.isEmpty()) {
                // if policy is configured for the endpoint, generate a WSDL with the policy attachments
                BindingID bindingId = bindingIdResolver.resolveBindingId(intentNames);
                QName name = endpointDefinition.getServiceName();
                GeneratedArtifacts artifacts = wsdlGenerator.generate(serviceClass, name, REPLACEABLE_ADDRESS, bindingId);
                wsdl = artifacts.getWsdl();
                schemas = artifacts.getSchemas();
                wsdl = mergePolicy(wsdl, policyExpressions, mappings);
            } else if (endpointDefinition.getWsdl() != null) {
                // No policy specified, use the WSDL specified via wsdlElement or wsdlLocation. If one is not specified, wsdl will be generated from
                // introspecting the Java provider class. If the WSDL is specified, it will be used instead when the endpoint is created.
                wsdl = endpointDefinition.getWsdl();
            }
            URI classLoaderUri = null;
            if (serviceClass.getClassLoader() instanceof MultiParentClassLoader) {
                classLoaderUri = ((MultiParentClassLoader) serviceClass.getClassLoader()).getName();
            }
            URI serviceUri = null;
            if (binding.isCallback()) {
                LogicalComponent<?> component = binding.getParent().getParent();
                for (LogicalService service : component.getServices()) {
                    if (service.getServiceContract().getQualifiedInterfaceName().equals(contract.getQualifiedInterfaceName())) {
                        try {
                            serviceUri = new URI(component.getUri() + "#" + service.getDefinition().getName());
                        } catch (URISyntaxException e) {
                            throw new GenerationException(e);
                        }
                        break;
                    }
                }
            } else {
                serviceUri = binding.getParent().getUri();
            }

            boolean bidirectional = contract.getCallbackContract() != null && !binding.isCallback();

            return new MetroJavaWireSourceDefinition(serviceUri,
                                                     endpointDefinition,
                                                     interfaze,
                                                     generatedBytes,
                                                     classLoaderUri,
                                                     wsdl,
                                                     schemas,
                                                     intentNames,
                                                     wsdlLocation,
                                                     bidirectional,
                                                     handlers);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        //        }
    }

    public MetroWireTargetDefinition generateTarget(LogicalBinding<WsBindingDefinition> binding, JavaServiceContract contract, EffectivePolicy policy)
            throws GenerationException {
        URL targetUrl = null;
        WsBindingDefinition definition = binding.getDefinition();
        URI targetUri = definition.getTargetUri();

        if (binding.isCallback() && targetUri != null) {
            throw new GenerationException("A web services callback binding cannot be used with a binding URI on a service: " + binding.getParent().getUri());
        }

        if (targetUri != null) {
            if (!targetUri.isAbsolute() && !binding.isCallback()) {
                throw new GenerationException("Web service binding URI must be absolute on reference: " + binding.getParent().getUri());
            }
            try {
                targetUrl = targetUri.toURL();
            } catch (MalformedURLException e) {
                throw new GenerationException(e);
            }
        } else if (definition.getWsdlElement() == null && definition.getWsdlLocation() == null && !binding.isCallback()) {
            throw new GenerationException("A web service binding URI must be specified: " + binding.getParent().getUri());
        }

        return generateTarget(binding, targetUrl, contract, policy);
    }

    public MetroWireTargetDefinition generateServiceBindingTarget(LogicalBinding<WsBindingDefinition> serviceBinding,
                                                                  JavaServiceContract contract,
                                                                  EffectivePolicy policy) throws GenerationException {
        URL targetUrl = targetUrlResolver.resolveUrl(serviceBinding, policy);
        return generateTarget(serviceBinding, targetUrl, contract, policy);
    }

    private MetroWireTargetDefinition generateTarget(LogicalBinding<WsBindingDefinition> binding,
                                                     URL targetUrl,
                                                     JavaServiceContract contract,
                                                     EffectivePolicy policy) throws GenerationException {
        URI contributionUri = binding.getParent().getParent().getDefinition().getContributionUri();
        Class<?> serviceClass = loadServiceClass(contract, contributionUri);
        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition, serviceClass);

        ReferenceEndpointDefinition endpointDefinition = createReferenceEndpointDefinition(binding, contract, serviceClass, targetUrl);

        String interfaze = contract.getQualifiedInterfaceName();

        Set<Intent> endpointIntents = policy.getProvidedEndpointIntents();
        List<QName> intentNames = new ArrayList<>();
        for (Intent intent : endpointIntents) {
            intentNames.add(intent.getName());
        }

        // handle endpoint-level policies
        List<Element> policyExpressions = new ArrayList<>();
        for (PolicySet policySet : policy.getEndpointPolicySets()) {
            Element expression = policySet.getExpression();
            if (expression == null) {
                // empty policy set
                continue;
            }
            policyExpressions.add(expression);
        }

        // Note operation level provided intents are not currently supported. Intents are mapped to JAX-WS features, which are per endpoint.

        // map operation-level policies
        List<PolicyExpressionMapping> mappings = GenerationHelper.createMappings(policy, serviceClass);

        List<PhysicalBindingHandlerDefinition> handlers = GenerationHelper.generateBindingHandlers(info.getDomain(), definition);

        byte[] generatedBytes = null;
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
                generatedBytes = generatedInterface.getBytes();
                serviceClass = generatedInterface.getGeneratedClass();
                interfaze = serviceClass.getName();
            }
            if (!policyExpressions.isEmpty() || !mappings.isEmpty()) {
                // if policy is configured for the endpoint, generate a WSDL with the policy attachments
                BindingID bindingId = bindingIdResolver.resolveBindingId(intentNames);
                QName name = endpointDefinition.getServiceName();
                String address = endpointDefinition.getUrl().toString();
                GeneratedArtifacts artifacts = wsdlGenerator.generate(serviceClass, name, address, bindingId);
                wsdl = artifacts.getWsdl();
                schemas = artifacts.getSchemas();
                // use the WSDL merged with policy and not a WSDL specified via wsdlElement or wsdlLocation
                wsdl = mergePolicy(wsdl, policyExpressions, mappings);
            } else if (endpointDefinition.getWsdl() != null) {
                // No policy specified, use the WSDL specified via wsdlElement or wsdlLocation. If one is not specified, wsdl will be null and
                // one will be downloaded from the endpoint address (?wsdl) when the reference proxy is created on a runtime and cached. If the
                // WSDL is specified, it will be used instead when the reference proxy is created.
                wsdl = endpointDefinition.getWsdl();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        // obtain security information
        SecurityConfiguration securityConfiguration = GenerationHelper.createSecurityConfiguration(definition);

        // obtain connection information
        ConnectionConfiguration connectionConfiguration = GenerationHelper.createConnectionConfiguration(definition);

        URI classLoaderUri = null;
        if (serviceClass.getClassLoader() instanceof MultiParentClassLoader) {
            classLoaderUri = ((MultiParentClassLoader) serviceClass.getClassLoader()).getName();
        }

        boolean bidirectional = contract.getCallbackContract() != null && !binding.isCallback();

        int retries = definition.getRetries();
        MetroJavaWireTargetDefinition targetDefinition = new MetroJavaWireTargetDefinition(endpointDefinition,
                                                                                           interfaze,
                                                                                           generatedBytes,
                                                                                           classLoaderUri,
                                                                                           wsdl,
                                                                                           schemas,
                                                                                           wsdlLocation,
                                                                                           intentNames,
                                                                                           securityConfiguration,
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
     * @throws GenerationException if the WSDL location is invalid
     */
    private URL getWsdlLocation(WsBindingDefinition definition, Class<?> serviceClass) throws GenerationException {
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
            throw new GenerationException(e);
        }
        return null;

    }

    /**
     * Loads a service contract class in either a host environment that supports classloader isolation or one that does not, in which case the TCCL is used.
     *
     * @param javaContract    the contract
     * @param contributionUri the          contribution URI the contract class is loaded in
     * @return the loaded class
     */
    private Class<?> loadServiceClass(JavaServiceContract javaContract, URI contributionUri) throws GenerationException {
        ClassLoader loader = classLoaderRegistry.getClassLoader(contributionUri);
        try {
            return loader.loadClass(javaContract.getInterfaceClass());
        } catch (ClassNotFoundException e) {
            throw new GenerationException(e);
        }
    }

    private ServiceEndpointDefinition createServiceEndpointDefinition(LogicalBinding<WsBindingDefinition> binding,
                                                                      JavaServiceContract contract,
                                                                      Class<?> serviceClass) throws GenerationException {
        URI targetUri = binding.getDefinition().getTargetUri();
        if (targetUri == null) {
            targetUri = URI.create(binding.getParent().getUri().getFragment());  // use the service URI fragment
        }
        return synthesizer.synthesizeServiceEndpoint(contract, serviceClass, targetUri);
    }

    /**
     * Merges policy sets into the given WSDL document.
     *
     * @param wsdl              the WSDL
     * @param policyExpressions the policy set expressions
     * @param mappings          policy set to operation mappings
     * @return the merged WSDL
     * @throws GenerationException if the merge is unsuccessful
     */
    private String mergePolicy(String wsdl, List<Element> policyExpressions, List<PolicyExpressionMapping> mappings) throws GenerationException {
        try {
            Document wsdlDocument = documentBuilder.parse(new ByteArrayInputStream(wsdl.getBytes()));
            policyAttacher.attach(wsdlDocument, policyExpressions, mappings);
            // Write the DOM representing the abstract WSDL back to the file
            Source source = new DOMSource(wsdlDocument);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
            return writer.toString();
        } catch (IOException | TransformerException | SAXException e) {
            throw new GenerationException(e);
        }
    }

    private ReferenceEndpointDefinition createReferenceEndpointDefinition(LogicalBinding<WsBindingDefinition> binding,
                                                                          JavaServiceContract contract,
                                                                          Class<?> serviceClass,
                                                                          URL targetUrl) throws GenerationException {
        if (binding.isCallback()) {
            targetUrl = ReferenceEndpointDefinition.DYNAMIC_URL;
        }

        if (targetUrl != null) {
            return synthesizer.synthesizeReferenceEndpoint(contract, serviceClass, targetUrl);
        } else {
            throw new GenerationException("Target URL must be specified");
        }
    }

}
