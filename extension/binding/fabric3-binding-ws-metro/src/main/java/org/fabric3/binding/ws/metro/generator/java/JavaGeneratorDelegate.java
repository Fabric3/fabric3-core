/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

package org.fabric3.binding.ws.metro.generator.java;

import javax.jws.WebService;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.xml.ws.api.BindingID;
import org.fabric3.binding.ws.metro.generator.GenerationHelper;
import org.fabric3.binding.ws.metro.generator.MetroGeneratorDelegate;
import org.fabric3.binding.ws.metro.generator.PolicyExpressionMapping;
import org.fabric3.binding.ws.metro.generator.WsdlElement;
import org.fabric3.binding.ws.metro.generator.java.codegen.GeneratedInterface;
import org.fabric3.binding.ws.metro.generator.java.codegen.InterfaceGenerator;
import org.fabric3.binding.ws.metro.generator.java.wsdl.GeneratedArtifacts;
import org.fabric3.binding.ws.metro.generator.java.wsdl.JavaWsdlGenerator;
import org.fabric3.binding.ws.metro.generator.policy.WsdlPolicyAttacher;
import org.fabric3.binding.ws.metro.generator.resolver.EndpointResolutionException;
import org.fabric3.binding.ws.metro.generator.resolver.EndpointResolver;
import org.fabric3.binding.ws.metro.generator.resolver.TargetUrlResolver;
import org.fabric3.binding.ws.metro.generator.resolver.WsdlResolutionException;
import org.fabric3.binding.ws.metro.generator.resolver.WsdlResolver;
import org.fabric3.binding.ws.metro.generator.validator.WsdlEndpointValidator;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroJavaSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroJavaTargetDefinition;
import org.fabric3.binding.ws.metro.provision.MetroTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.util.BindingIdResolver;
import org.fabric3.binding.ws.metro.util.ClassLoaderUpdater;
import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.AbstractService;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.wsdl.model.WsdlServiceContract;
import org.oasisopen.sca.annotation.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Generates source and target definitions for an endpoint defined by a Java-based service contract.
 */
public class JavaGeneratorDelegate implements MetroGeneratorDelegate<JavaServiceContract> {
    private static final String REPLACEABLE_ADDRESS = "REPLACE_WITH_ACTUAL_URL";
    private WsdlResolver wsdlResolver;
    private EndpointResolver endpointResolver;
    private EndpointSynthesizer synthesizer;
    private JavaWsdlGenerator wsdlGenerator;
    private InterfaceGenerator interfaceGenerator;
    private BindingIdResolver bindingIdResolver;
    private WsdlPolicyAttacher policyAttacher;
    private ClassLoaderRegistry classLoaderRegistry;
    private ClassLoaderUpdater classLoaderUpdater;
    private TargetUrlResolver targetUrlResolver;
    private WsdlEndpointValidator endpointValidator;
    private HostInfo info;
    private DocumentBuilder documentBuilder;
    private TransformerFactory transformerFactory;

    public JavaGeneratorDelegate(@Reference WsdlResolver wsdlResolver,
                                 @Reference EndpointResolver endpointResolver,
                                 @Reference EndpointSynthesizer synthesizer,
                                 @Reference JavaWsdlGenerator wsdlGenerator,
                                 @Reference InterfaceGenerator interfaceGenerator,
                                 @Reference BindingIdResolver bindingIdResolver,
                                 @Reference WsdlPolicyAttacher policyAttacher,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference ClassLoaderUpdater classLoaderUpdater,
                                 @Reference TargetUrlResolver targetUrlResolver,
                                 @Reference WsdlEndpointValidator endpointValidator,
                                 @Reference HostInfo info) throws ParserConfigurationException {
        this.wsdlResolver = wsdlResolver;
        this.endpointResolver = endpointResolver;
        this.synthesizer = synthesizer;
        this.wsdlGenerator = wsdlGenerator;
        this.interfaceGenerator = interfaceGenerator;
        this.bindingIdResolver = bindingIdResolver;
        this.policyAttacher = policyAttacher;
        this.classLoaderRegistry = classLoaderRegistry;
        this.classLoaderUpdater = classLoaderUpdater;
        this.targetUrlResolver = targetUrlResolver;
        this.endpointValidator = endpointValidator;
        this.info = info;
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        transformerFactory = TransformerFactory.newInstance();
    }

    public MetroJavaSourceDefinition generateSource(LogicalBinding<WsBindingDefinition> binding, JavaServiceContract contract, EffectivePolicy policy)
            throws GenerationException {

        Class<?> serviceClass = loadServiceClass(contract);
        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition, serviceClass);

        ServiceEndpointDefinition endpointDefinition = createServiceEndpointDefinition(binding, contract, serviceClass, wsdlLocation);

        String interfaze = contract.getQualifiedInterfaceName();

        // handle endpoint-level intents provided by Metro
        List<QName> intentNames = new ArrayList<QName>();
        Set<Intent> endpointIntents = policy.getEndpointIntents();
        for (Intent intent : endpointIntents) {
            intentNames.add(intent.getName());
        }

        // handle endpoint-level policies
        List<Element> policyExpressions = new ArrayList<Element>();
        for (PolicySet policySet : policy.getEndpointPolicySets()) {
            policyExpressions.add(policySet.getExpression());
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
            return new MetroJavaSourceDefinition(endpointDefinition, interfaze, generatedBytes, classLoaderUri, wsdl, schemas, intentNames, wsdlLocation,
                                                 handlers);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public MetroTargetDefinition generateTarget(LogicalBinding<WsBindingDefinition> binding, JavaServiceContract contract, EffectivePolicy policy)
            throws GenerationException {
        URL targetUrl = null;
        WsBindingDefinition definition = binding.getDefinition();
        URI targetUri = definition.getTargetUri();

        if (binding.isCallback() && targetUri != null) {
            throw new GenerationException("A web services callback binding cannot be used with a binding URI on a service: " + binding.getParent().getUri());
        }

        if (targetUri != null) {
            if (!targetUri.isAbsolute()) {
                throw new GenerationException("Web service binding URI must be absolute on reference: " + binding.getParent().getUri());
            }
            try {
                targetUrl = targetUri.toURL();
            } catch (MalformedURLException e) {
                throw new GenerationException(e);
            }
        } else if (definition.getWsdlElement() == null && definition.getWsdlLocation() == null) {
            throw new GenerationException("A web service binding URI must be specified: " + binding.getParent().getUri());
        }

        return generateTarget(binding, targetUrl, contract, policy);
    }

    public MetroTargetDefinition generateServiceBindingTarget(LogicalBinding<WsBindingDefinition> serviceBinding,
                                                              JavaServiceContract contract,
                                                              EffectivePolicy policy) throws GenerationException {
        URL targetUrl = targetUrlResolver.resolveUrl(serviceBinding, policy);
        return generateTarget(serviceBinding, targetUrl, contract, policy);
    }

    private MetroTargetDefinition generateTarget(LogicalBinding<WsBindingDefinition> binding,
                                                 URL targetUrl,
                                                 JavaServiceContract contract,
                                                 EffectivePolicy policy) throws GenerationException {
        Class<?> serviceClass = loadServiceClass(contract);
        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition, serviceClass);

        ReferenceEndpointDefinition endpointDefinition = createReferenceEndpointDefinition(binding, contract, serviceClass, targetUrl, wsdlLocation);

        String interfaze = contract.getQualifiedInterfaceName();

        Set<Intent> endpointIntents = policy.getEndpointIntents();
        List<QName> intentNames = new ArrayList<QName>();
        for (Intent intent : endpointIntents) {
            intentNames.add(intent.getName());
        }

        // handle endpoint-level policies
        List<Element> policyExpressions = new ArrayList<Element>();
        for (PolicySet policySet : policy.getEndpointPolicySets()) {
            policyExpressions.add(policySet.getExpression());
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

        int retries = definition.getRetries();
        return new MetroJavaTargetDefinition(endpointDefinition, interfaze, generatedBytes, classLoaderUri, wsdl, schemas, wsdlLocation, intentNames,
                                             securityConfiguration, connectionConfiguration, retries, handlers);
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
     * @param javaContract the contract
     * @return the loaded class
     */
    private Class<?> loadServiceClass(JavaServiceContract javaContract) {
        ClassLoader loader;
        if (info.supportsClassLoaderIsolation()) {
            URI classLoaderUri = javaContract.getContributionClassLoaderUri();
            if (classLoaderUri == null) {
                loader = Thread.currentThread().getContextClassLoader();
            } else {
                loader = classLoaderRegistry.getClassLoader(classLoaderUri);
                if (loader == null) {
                    // programming error
                    throw new AssertionError("Classloader not found: " + classLoaderUri);
                }
            }
        } else {
            loader = Thread.currentThread().getContextClassLoader();
        }
        Class<?> clazz;
        try {
            clazz = loader.loadClass(javaContract.getInterfaceClass());
        } catch (ClassNotFoundException e) {
            // programming error
            throw new AssertionError(e);
        }
        return clazz;
    }

    private ServiceEndpointDefinition createServiceEndpointDefinition(LogicalBinding<WsBindingDefinition> binding,
                                                                      JavaServiceContract contract,
                                                                      Class<?> serviceClass,
                                                                      URL wsdlLocation) throws GenerationException {
        ServiceEndpointDefinition endpointDefinition;
        URI targetUri = binding.getDefinition().getTargetUri();
        if (targetUri != null) {
            endpointDefinition = synthesizer.synthesizeServiceEndpoint(contract, serviceClass, targetUri);
        } else {
            // no target uri specified, check wsdlElement
            String wsdlElementString = binding.getDefinition().getWsdlElement();
            if (wsdlElementString == null) {
                // check if interface.wsdl is used
                AbstractService abstractService = (AbstractService) binding.getDefinition().getParent();
                if (abstractService.getServiceContract() instanceof WsdlServiceContract) {
                    return synthesizeEndpointFromWsdlInterface(binding, abstractService);
                } else {
                    // the WSDL element is not specified, default to the service name
                    return synthesizeEndpointFromClass(binding, contract, serviceClass);
                }
            }
            WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(wsdlElementString);
            if (WsdlElement.Type.SERVICE == wsdlElement.getType()) {
                throw new GenerationException("Services cannot specify a wsdl.service in the web service binding: " + binding.getParent().getUri());
            }

            if (wsdlLocation == null) {
                URI contributionUri = binding.getParent().getParent().getDefinition().getContributionUri();
                if (WsdlElement.Type.BINDING == wsdlElement.getType()) {
                    // binding element, validate and then generate from class
                    endpointValidator.validateBinding(contributionUri, binding, wsdlElement.getBindingName());
                    return synthesizeEndpointFromClass(binding, contract, serviceClass);
                } else {
                    Definition wsdl = wsdlResolver.resolveWsdlByPortName(contributionUri, wsdlElement.getPortName());
                    endpointDefinition = endpointResolver.resolveServiceEndpoint(wsdlElement, wsdl);
                    endpointValidator.validate(contributionUri, binding, endpointDefinition);
                }
            } else {
                Definition wsdl = wsdlResolver.parseWsdl(wsdlLocation);
                endpointDefinition = endpointResolver.resolveServiceEndpoint(wsdlElement, wsdl);
            }
        }
        return endpointDefinition;
    }

    private ServiceEndpointDefinition synthesizeEndpointFromWsdlInterface(LogicalBinding<WsBindingDefinition> binding, AbstractService abstractService)
            throws EndpointResolutionException {
        URI targetUri;
        WsdlServiceContract wsdlContract = (WsdlServiceContract) abstractService.getServiceContract();
        Definition wsdl = wsdlContract.getDefinition();
        QName portType = wsdlContract.getPortType().getQName();
        QName serviceName = new QName(portType.getNamespaceURI(), portType.getLocalPart() + "Service");
        QName portName = new QName(portType.getNamespaceURI(), portType.getLocalPart() + "Port");
        Bindable service = binding.getParent();
        targetUri = URI.create(service.getUri().getFragment());
        String serializedWsdl = endpointResolver.serializeWsdl(wsdl);
        return new ServiceEndpointDefinition(serviceName, portName, targetUri, serializedWsdl);
    }

    private ServiceEndpointDefinition synthesizeEndpointFromClass(LogicalBinding<WsBindingDefinition> binding,
                                                                  JavaServiceContract contract,
                                                                  Class<?> serviceClass) throws GenerationException {
        URI targetUri;
        Bindable service = binding.getParent();
        for (LogicalBinding<?> otherBinding : service.getBindings()) {
            if (binding == otherBinding) {
                continue;
            }
            if (WsBindingDefinition.BINDING_QNAME.equals(otherBinding.getDefinition().getType())) {
                // check to see if other WS bindings also use a default
                WsBindingDefinition wsDefinition = (WsBindingDefinition) otherBinding.getDefinition();
                if (wsDefinition.getTargetUri() == null && wsDefinition.getWsdlElement() == null) {
                    throw new GenerationException("If there is more than one web service binding, one must provide a URI or WSDLElement:" + service.getUri());
                }

            }
        }
        targetUri = URI.create(service.getUri().getFragment());
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
        } catch (IOException e) {
            throw new GenerationException(e);
        } catch (SAXException e) {
            throw new GenerationException(e);
        } catch (TransformerException e) {
            throw new GenerationException(e);
        }
    }

    private ReferenceEndpointDefinition createReferenceEndpointDefinition(LogicalBinding<WsBindingDefinition> binding,
                                                                          JavaServiceContract contract,
                                                                          Class<?> serviceClass,
                                                                          URL targetUrl,
                                                                          URL wsdlLocation) throws GenerationException {
        WsBindingDefinition definition = binding.getDefinition();
        ReferenceEndpointDefinition endpointDefinition;

        URI contributionUri = getContributionUri(binding);

        if (targetUrl != null) {
            if (definition.getWsdlElement() != null) {
                // wsdl binding specified, use that port type
                WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(definition.getWsdlElement());
                if (WsdlElement.Type.BINDING != wsdlElement.getType()) {
                    throw new GenerationException("Cannot specify a target URI and non-binding wsdlElement: " + binding.getParent().getUri());
                }
                QName bindingName = wsdlElement.getBindingName();
                Definition wsdl = resolveWsdl(wsdlLocation, contributionUri, bindingName);
                Binding wsdlBinding = wsdl.getBinding(bindingName);
                QName portTypeName = wsdlBinding.getPortType().getQName();
                endpointDefinition = synthesizer.synthesizeReferenceEndpoint(contract, serviceClass, portTypeName, targetUrl);
                endpointValidator.validateBinding(contributionUri, binding, bindingName);
            } else {
                endpointDefinition = synthesizer.synthesizeReferenceEndpoint(contract, serviceClass, targetUrl);
            }
        } else {
            // no target uri specified, introspect from wsdlElement
            WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(definition.getWsdlElement());
            if (wsdlLocation == null) {
                // if the WSDL location is not specified, resolve against the contribution imports
                Definition wsdl = resolveWsdl(contributionUri, wsdlElement);
                endpointDefinition = endpointResolver.resolveReferenceEndpoint(wsdlElement, wsdl);
                endpointValidator.validate(contributionUri, binding, endpointDefinition);
            } else {
                // a specific WSDL location is specified
                Definition wsdl = wsdlResolver.parseWsdl(wsdlLocation);
                endpointDefinition = endpointResolver.resolveReferenceEndpoint(wsdlElement, wsdl);
            }

        }
        return endpointDefinition;
    }

    private Definition resolveWsdl(URI contributionUri, WsdlElement wsdlElement) throws WsdlResolutionException {
        Definition wsdl;
        if (WsdlElement.Type.PORT == wsdlElement.getType()) {
            wsdl = wsdlResolver.resolveWsdlByPortName(contributionUri, wsdlElement.getPortName());
        } else if (WsdlElement.Type.SERVICE == wsdlElement.getType()) {
            wsdl = wsdlResolver.resolveWsdlByServiceName(contributionUri, wsdlElement.getServiceName());
        } else {
            wsdl = wsdlResolver.resolveWsdlByBindingName(contributionUri, wsdlElement.getBindingName());
        }
        return wsdl;
    }

    private Definition resolveWsdl(URL wsdlLocation, URI contributionUri, QName bindingName) throws WsdlResolutionException {
        Definition wsdl;
        if (wsdlLocation != null) {
            wsdl = wsdlResolver.parseWsdl(wsdlLocation);
        } else {
            wsdl = wsdlResolver.resolveWsdlByBindingName(contributionUri, bindingName);
        }
        return wsdl;
    }

    private URI getContributionUri(LogicalBinding<WsBindingDefinition> binding) {
        LogicalComponent<?> current = binding.getParent().getParent();

        while (current.getParent().getParent() != null) {  // component deployed directly to the domain
            current = current.getParent();
        }
        return current.getDefinition().getContributionUri();
    }

}
