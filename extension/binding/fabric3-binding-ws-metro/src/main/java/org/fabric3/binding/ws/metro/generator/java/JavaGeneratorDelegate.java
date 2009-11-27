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

package org.fabric3.binding.ws.metro.generator.java;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jws.WebService;
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

import com.sun.xml.ws.api.BindingID;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.fabric3.binding.ws.metro.generator.GenerationHelper;
import org.fabric3.binding.ws.metro.generator.MetroGeneratorDelegate;
import org.fabric3.binding.ws.metro.generator.PolicyExpressionMapping;
import org.fabric3.binding.ws.metro.generator.WsdlElement;
import org.fabric3.binding.ws.metro.generator.java.codegen.GeneratedInterface;
import org.fabric3.binding.ws.metro.generator.java.codegen.InterfaceGenerator;
import org.fabric3.binding.ws.metro.generator.java.wsdl.GeneratedArtifacts;
import org.fabric3.binding.ws.metro.generator.java.wsdl.JavaWsdlGenerator;
import org.fabric3.binding.ws.metro.generator.policy.WsdlPolicyAttacher;
import org.fabric3.binding.ws.metro.generator.resolver.EndpointResolver;
import org.fabric3.binding.ws.metro.generator.resolver.WsdlResolver;
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
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.policy.EffectivePolicy;

/**
 * Generates source and target definitions for an endpoint defined by a Java-based service contract.
 *
 * @version $Rev$ $Date$
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
        this.info = info;
        documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        transformerFactory = TransformerFactory.newInstance();
    }

    public MetroJavaSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                        JavaServiceContract contract,
                                                        EffectivePolicy policy) throws GenerationException {
        Class<?> serviceClass = loadServiceClass(binding, contract);
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

        byte[] generatedBytes = null;
        String wsdl = null;
        Map<String, String> schemas = null;

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
            }
            return new MetroJavaSourceDefinition(endpointDefinition, interfaze, generatedBytes, wsdl, schemas, intentNames, wsdlLocation);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public MetroTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                    JavaServiceContract contract,
                                                    EffectivePolicy policy) throws GenerationException {
        Class<?> serviceClass = loadServiceClass(binding, contract);
        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition, serviceClass);

        ReferenceEndpointDefinition endpointDefinition = createReferenceEndpointDefinition(binding, contract, serviceClass, wsdlLocation);

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

        byte[] generatedBytes = null;
        String wsdl = null;
        Map<String, String> schemas = null;

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
                wsdl = mergePolicy(wsdl, policyExpressions, mappings);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        // obtain security information
        SecurityConfiguration securityConfiguration = GenerationHelper.createSecurityConfiguration(definition);

        // obtain connection information
        ConnectionConfiguration connectionConfiguration = GenerationHelper.createConnectionConfiguration(definition);

        return new MetroJavaTargetDefinition(endpointDefinition,
                                             interfaze,
                                             generatedBytes,
                                             wsdl,
                                             schemas,
                                             wsdlLocation,
                                             intentNames,
                                             securityConfiguration,
                                             connectionConfiguration);
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
     * Loads a service contract class in either a host environment that supports classloader isolation or one that does not, in which case the TCCL is
     * used.
     *
     * @param binding      the binding defintion
     * @param javaContract the contract
     * @return the loaded class
     */
    private Class<?> loadServiceClass(LogicalBinding<WsBindingDefinition> binding, JavaServiceContract javaContract) {
        ClassLoader loader;
        if (info.supportsClassLoaderIsolation()) {
            URI classLoaderUri = binding.getParent().getParent().getDefinition().getContributionUri();
            // check if a namespace is assigned
            loader = classLoaderRegistry.getClassLoader(classLoaderUri);
            if (loader == null) {
                // programming error
                throw new AssertionError("Classloader not found: " + classLoaderUri);
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
                                                                      Class<?> serviceClass, URL wsdlLocation) throws GenerationException {
        ServiceEndpointDefinition endpointDefinition;
        URI targetUri = binding.getDefinition().getTargetUri();
        if (targetUri != null) {
            endpointDefinition = synthesizer.synthesizeServiceEndpoint(contract, serviceClass, targetUri);
        } else {
            // no target uri specified, check wsdlElement
            String wsdlElementString = binding.getDefinition().getWsdlElement();
            if (wsdlElementString == null) {
                URI bindableUri = binding.getParent().getUri();
                throw new GenerationException("Either a uri or wsdlElement must be specified for the web service binding on " + bindableUri);
            }
            WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(wsdlElementString);
            if (wsdlLocation == null) {
                URI contributionUri = binding.getParent().getParent().getDefinition().getContributionUri();
                Definition wsdl = wsdlResolver.resolveWsdlByPortName(contributionUri, wsdlElement.getPortName());
                endpointDefinition = endpointResolver.resolveServiceEndpoint(wsdlElement, wsdl);
            } else {
                Definition wsdl = wsdlResolver.parseWsdl(wsdlLocation);
                endpointDefinition = endpointResolver.resolveServiceEndpoint(wsdlElement, wsdl);
            }
        }
        return endpointDefinition;
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
                                                                          URL wsdlLocation) throws GenerationException {
        WsBindingDefinition definition = binding.getDefinition();
        ReferenceEndpointDefinition endpointDefinition;
        URI targetUri = definition.getTargetUri();
        if (targetUri != null) {
            try {
                // TODO get rid of need to decode
                URL url = new URL(URLDecoder.decode(targetUri.toASCIIString(), "UTF-8"));
                endpointDefinition = synthesizer.synthesizeReferenceEndpoint(contract, serviceClass, url);
            } catch (MalformedURLException e) {
                throw new GenerationException(e);
            } catch (UnsupportedEncodingException e) {
                throw new GenerationException(e);
            }
        } else {
            // no target uri specified, introspect from wsdlElement
            WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(definition.getWsdlElement());
            if (wsdlLocation == null) {
                URI contributionUri = binding.getParent().getParent().getDefinition().getContributionUri();
                Definition wsdl = wsdlResolver.resolveWsdlByPortName(contributionUri, wsdlElement.getPortName());
                endpointDefinition = endpointResolver.resolveReferenceEndpoint(wsdlElement, wsdl);
            } else {
                Definition wsdl = wsdlResolver.parseWsdl(wsdlLocation);
                endpointDefinition = endpointResolver.resolveReferenceEndpoint(wsdlElement, wsdl);
            }

        }
        return endpointDefinition;
    }

}
