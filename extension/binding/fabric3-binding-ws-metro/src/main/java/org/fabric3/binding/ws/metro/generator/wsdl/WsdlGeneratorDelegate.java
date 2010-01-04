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

package org.fabric3.binding.ws.metro.generator.wsdl;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.metro.generator.GenerationHelper;
import org.fabric3.binding.ws.metro.generator.MetroGeneratorDelegate;
import org.fabric3.binding.ws.metro.generator.PolicyExpressionMapping;
import org.fabric3.binding.ws.metro.generator.WsdlElement;
import org.fabric3.binding.ws.metro.generator.policy.WsdlPolicyAttacher;
import org.fabric3.binding.ws.metro.generator.resolver.EndpointResolver;
import org.fabric3.binding.ws.metro.generator.resolver.TargetUrlResolver;
import org.fabric3.binding.ws.metro.generator.resolver.WsdlResolver;
import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.MetroSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroTargetDefinition;
import org.fabric3.binding.ws.metro.provision.MetroWsdlSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroWsdlTargetDefinition;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.policy.EffectivePolicy;
import org.fabric3.wsdl.factory.Wsdl4JFactory;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * Generates source and target definitions for an endpoint defined by a WSDL-based service contract.
 *
 * @version $Rev$ $Date$
 */
public class WsdlGeneratorDelegate implements MetroGeneratorDelegate<WsdlServiceContract> {
    private static final String REPLACEABLE_ADDRESS = "REPLACE_WITH_ACTUAL_URL";
    private WsdlResolver wsdlResolver;
    private EndpointResolver endpointResolver;
    private WsdlSynthesizer wsdlSynthesizer;
    private WsdlPolicyAttacher policyAttacher;
    private Wsdl4JFactory wsdlFactory;
    private TargetUrlResolver targetUrlResolver;
    private TransformerFactory transformerFactory;

    public WsdlGeneratorDelegate(@Reference WsdlResolver wsdlResolver,
                                 @Reference EndpointResolver endpointResolver,
                                 @Reference WsdlSynthesizer wsdlSynthesizer,
                                 @Reference WsdlPolicyAttacher policyAttacher,
                                 @Reference Wsdl4JFactory wsdlFactory,
                                 @Reference TargetUrlResolver targetUrlResolver) throws WSDLException {
        this.wsdlResolver = wsdlResolver;
        this.endpointResolver = endpointResolver;
        this.wsdlSynthesizer = wsdlSynthesizer;
        this.policyAttacher = policyAttacher;
        this.wsdlFactory = wsdlFactory;
        this.targetUrlResolver = targetUrlResolver;
        transformerFactory = TransformerFactory.newInstance();
    }

    public MetroSourceDefinition generateSource(LogicalBinding<WsBindingDefinition> binding, WsdlServiceContract contract, EffectivePolicy policy)
            throws GenerationException {
        URI targetUri = binding.getDefinition().getTargetUri();
        Definition wsdl;
        URL wsdlLocation = getWsdlLocation(binding);
        if (wsdlLocation != null) {
            wsdl = wsdlResolver.parseWsdl(wsdlLocation);
        } else {
            URI contributionUri = binding.getParent().getParent().getDefinition().getContributionUri();
            QName wsdlName = contract.getWsdlQName();
            wsdl = wsdlResolver.resolveWsdl(contributionUri, wsdlName);
        }

        ServiceEndpointDefinition endpointDefinition;
        if (targetUri != null) {
            String wsdlElementString = binding.getDefinition().getWsdlElement();
            if (wsdlElementString != null) {
                WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(wsdlElementString);
                endpointDefinition = endpointResolver.resolveServiceEndpoint(wsdlElement, wsdl, targetUri);
            } else {
                // A port type is used. Synthesize concrete WSDL for the port type.
                ConcreateWsdlResult result = wsdlSynthesizer.synthesize(binding, REPLACEABLE_ADDRESS, contract, policy, wsdl, targetUri);
                wsdl = result.getDefiniton();
                endpointDefinition = new ServiceEndpointDefinition(result.getServiceName(), result.getPortName(), targetUri);
            }
        } else {
            // no target uri specified, check wsdlElement
            String wsdlElementString = binding.getDefinition().getWsdlElement();
            if (wsdlElementString == null) {
                // A port type is used. Synthesize concrete WSDL for the port type.
                Bindable service = binding.getParent();
                targetUri = URI.create(service.getUri().getFragment());
                ConcreateWsdlResult result = wsdlSynthesizer.synthesize(binding, REPLACEABLE_ADDRESS, contract, policy, wsdl, targetUri);
                wsdl = result.getDefiniton();
                QName serviceName = result.getServiceName();
                endpointDefinition = new ServiceEndpointDefinition(serviceName, result.getPortName(), targetUri);
            } else {
                WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(wsdlElementString);
                endpointDefinition = endpointResolver.resolveServiceEndpoint(wsdlElement, wsdl, targetUri);
            }
        }

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

        String serializedWsdl;

        // Note operation level provided intents are not currently supported. Intents are mapped to JAX-WS features, which are per endpoint.
        List<PolicyExpressionMapping> mappings = GenerationHelper.createMappings(policy);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            // SAAJ classes need to be present on TCCL
            if (!policyExpressions.isEmpty() || !mappings.isEmpty()) {
                // if policy is configured for the endpoint, generate a WSDL with the policy attachments
                serializedWsdl = mergePolicy(wsdl, policyExpressions, mappings);
            } else {
                serializedWsdl = serializeToString(wsdl);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        return new MetroWsdlSourceDefinition(endpointDefinition, serializedWsdl, intentNames);
    }

    public MetroTargetDefinition generateTarget(LogicalBinding<WsBindingDefinition> binding,
                                                WsdlServiceContract contract,
                                                EffectivePolicy policy) throws GenerationException {
        URL targetUrl = null;
        URI targetUri = binding.getDefinition().getTargetUri();

        if (targetUri != null) {
            try {
                targetUrl = targetUri.toURL();
            } catch (MalformedURLException e) {
                throw new GenerationException(e);
            }
        }
        return generateTarget(binding, targetUrl, contract, policy);
    }

    public MetroTargetDefinition generateServiceBindingTarget(LogicalBinding<WsBindingDefinition> serviceBinding,
                                                              WsdlServiceContract contract,
                                                              EffectivePolicy policy) throws GenerationException {
        URL targetUrl = targetUrlResolver.resolveUrl(serviceBinding, policy);
        return generateTarget(serviceBinding, targetUrl, contract, policy);

    }

    private MetroTargetDefinition generateTarget(LogicalBinding<WsBindingDefinition> binding,
                                                 URL targetUrl,
                                                 WsdlServiceContract contract,
                                                 EffectivePolicy policy) throws GenerationException {
        WsBindingDefinition definition = binding.getDefinition();
        ReferenceEndpointDefinition endpointDefinition;
        URL wsdlLocation = getWsdlLocation(binding);
        Definition wsdl;
        if (wsdlLocation != null) {
            wsdl = wsdlResolver.parseWsdl(wsdlLocation);
        } else {
            URI contributionUri = binding.getParent().getParent().getDefinition().getContributionUri();
            QName wsdlName = contract.getWsdlQName();
            wsdl = wsdlResolver.resolveWsdl(contributionUri, wsdlName);
        }
        if (targetUrl != null) {
            String wsdlElementString = binding.getDefinition().getWsdlElement();
            if (wsdlElementString != null) {
                WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(wsdlElementString);
                endpointDefinition = endpointResolver.resolveReferenceEndpoint(wsdlElement, wsdl);
            } else {
                // A port type is used. Synthesize concrete WSDL for the port type.
                String endpointAddress = targetUrl.toString();
                URI targetUri = binding.getDefinition().getTargetUri();
                ConcreateWsdlResult result = wsdlSynthesizer.synthesize(binding, endpointAddress, contract, policy, wsdl, targetUri);
                wsdl = result.getDefiniton();
                // FIXME null service name
                QName portTypeName = contract.getPortTypeQname();
                QName portName = result.getPortName();
                endpointDefinition = new ReferenceEndpointDefinition(result.getServiceName(), false, portName, portTypeName, targetUrl);
            }
        } else {
            // no target url specified, check wsdlElement
            String wsdlElementString = binding.getDefinition().getWsdlElement();
            if (wsdlElementString == null) {
                URI bindableUri = binding.getParent().getUri();
                throw new GenerationException("Either a uri or wsdlElement must be specified for the web service binding on " + bindableUri);
            }
            WsdlElement wsdlElement = GenerationHelper.parseWsdlElement(wsdlElementString);
            endpointDefinition = endpointResolver.resolveReferenceEndpoint(wsdlElement, wsdl);
        }

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
        List<PolicyExpressionMapping> mappings = GenerationHelper.createMappings(policy);

        String serializedWsdl;
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            // SAAJ classes need to be present on TCCL
            if (!policyExpressions.isEmpty() || !mappings.isEmpty()) {
                // if policy is configured for the endpoint, generate a WSDL with the policy attachments
                serializedWsdl = mergePolicy(wsdl, policyExpressions, mappings);
            } else {
                serializedWsdl = serializeToString(wsdl);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

        // obtain security information
        SecurityConfiguration securityConfiguration = GenerationHelper.createSecurityConfiguration(definition);

        // obtain connection information
        ConnectionConfiguration connectionConfiguration = GenerationHelper.createConnectionConfiguration(definition);

        return new MetroWsdlTargetDefinition(endpointDefinition,
                                             serializedWsdl,
                                             intentNames,
                                             securityConfiguration,
                                             connectionConfiguration);
    }


    /**
     * Returns the WSDL location if one is defined in the binding configuration or null.
     *
     * @param binding the logical binding
     * @return the WSDL location or null
     * @throws GenerationException if the WSDL location is invalid
     */
    private URL getWsdlLocation(LogicalBinding<WsBindingDefinition> binding) throws GenerationException {
        WsBindingDefinition definition = binding.getDefinition();
        try {
            String location = definition.getWsdlLocation();
            if (location != null) {
                return new URL(location);
            }
        } catch (MalformedURLException e) {
            throw new GenerationException(e);
        }
        return null;
    }

    /**
     * Serializes the contents of a parsed WSDL as a string.
     *
     * @param wsdl the WSDL
     * @return the serialized WSDL
     * @throws GenerationException if an error occurs reading the URL
     */
    private String serializeToString(Definition wsdl) throws GenerationException {
        try {
            WSDLWriter writer = wsdlFactory.newWriter();
            StringWriter stringWriter = new StringWriter();
            writer.writeWSDL(wsdl, stringWriter);
            return stringWriter.toString();
        } catch (WSDLException e) {
            throw new GenerationException(e);
        }
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
    private String mergePolicy(Definition wsdl, List<Element> policyExpressions, List<PolicyExpressionMapping> mappings) throws GenerationException {
        try {
            Document wsdlDocument = wsdlFactory.newWriter().getDocument(wsdl);
            policyAttacher.attach(wsdlDocument, policyExpressions, mappings);
            // Write the DOM representing the abstract WSDL back to the file
            Source source = new DOMSource(wsdlDocument);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
            return writer.toString();
        } catch (TransformerException e) {
            throw new GenerationException(e);
        } catch (WSDLException e) {
            throw new GenerationException(e);
        }
    }

}