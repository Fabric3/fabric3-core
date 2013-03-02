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

package org.fabric3.binding.ws.metro.generator;

import javax.jws.WebMethod;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.binding.ws.metro.provision.ConnectionConfiguration;
import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;
import org.fabric3.spi.model.type.binding.BindingHandlerDefinition;
import org.fabric3.spi.util.UriHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 */
public class GenerationHelper {
    private static final String WS_SECURITY_UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    private GenerationHelper() {
    }

    public static WsdlElement parseWsdlElement(String wsdlElement) throws WsdlElementParseException {
        if (wsdlElement == null) {
            throw new IllegalArgumentException("Null wsdlElement");
        }
        URI uri;
        try {
            uri = new URI(wsdlElement);
        } catch (URISyntaxException e) {
            throw new WsdlElementParseException(e);
        }
        String namespace = UriHelper.getDefragmentedNameAsString(uri);
        String fragment = uri.getFragment();
        if (fragment != null && fragment.startsWith("wsdl.port(")) {
            String name = fragment.substring(10, fragment.length() - 1); // wsdl.port(servicename/portname)
            String[] tokens = name.split("/");
            if (tokens.length != 2) {
                throw new WsdlElementParseException("Invalid wsdlElement expression: " + fragment);
            }
            QName serviceName = new QName(namespace, tokens[0]);
            QName portName = new QName(namespace, tokens[1]);

            return new WsdlElement(serviceName, portName);
        } else if (fragment != null && fragment.startsWith("wsdl.service(")) {
            String name = fragment.substring(13, fragment.length() - 1);
            QName serviceName = new QName(namespace, name);
            return new WsdlElement(serviceName, WsdlElement.Type.SERVICE);
        } else if (fragment != null && fragment.startsWith("wsdl.binding(")) {
            String name = fragment.substring(13, fragment.length() - 1);
            QName bindingName = new QName(namespace, name);
            return new WsdlElement(bindingName, WsdlElement.Type.BINDING);
        } else {
            throw new WsdlElementParseException("Expression not supported: " + fragment);
        }
    }

    /**
     * Maps policy expressions to the operations they are attached to.
     *
     * @param policy the policy for the wire
     * @return the policy expression mappings
     * @throws GenerationException if the policy expression is invalid
     */
    public static List<PolicyExpressionMapping> createMappings(EffectivePolicy policy) throws GenerationException {
        return createMappings(policy, null);
    }

    /**
     * Maps policy expressions to the operations they are attached to for a service contract defined by a JAX-WS interface.
     *
     * @param policy       the policy for the wire
     * @param serviceClass the service endpoint class
     * @return the policy expression mappings
     * @throws GenerationException if the policy expression is invalid
     */
    public static List<PolicyExpressionMapping> createMappings(EffectivePolicy policy, Class<?> serviceClass) throws GenerationException {
        // temporarily store mappings keyed by policy expression id
        Map<String, PolicyExpressionMapping> mappings = new HashMap<String, PolicyExpressionMapping>();
        for (Map.Entry<LogicalOperation, List<PolicySet>> entry : policy.getOperationPolicySets().entrySet()) {
            Operation definition = entry.getKey().getDefinition();
            for (PolicySet policySet : entry.getValue()) {
                Element expression = policySet.getExpression();
                Node node = expression.getAttributes().getNamedItemNS(WS_SECURITY_UTILITY_NS, "Id");
                if (node == null) {
                    URI uri = policySet.getContributionUri();
                    QName expressionName = policySet.getExpressionName();
                    throw new GenerationException("Invalid policy in contribution " + uri + ". No id specified: " + expressionName);
                }
                String id = node.getNodeValue();

                PolicyExpressionMapping mapping = mappings.get(id);
                if (mapping == null) {
                    mapping = new PolicyExpressionMapping(id, expression);
                    mappings.put(id, mapping);
                }
                String operationName;
                if (serviceClass == null) {
                    operationName = getWsdlName(definition, serviceClass);
                } else {
                    operationName = definition.getName();
                }
                mapping.addOperationName(operationName);
            }
        }
        return new ArrayList<PolicyExpressionMapping>(mappings.values());
    }

    /**
     * Parses security information and creates a security configuration.
     *
     * @param definition the binding definition
     * @return the security configuration
     */
    public static SecurityConfiguration createSecurityConfiguration(WsBindingDefinition definition) {
        SecurityConfiguration configuration = null;
        Map<String, String> configProperties = definition.getConfiguration();
        if (configProperties != null) {
            String alias = configProperties.get("alias");
            if (alias != null) {
                configuration = new SecurityConfiguration(alias);
            } else {
                String username = configProperties.get("username");
                String password = configProperties.get("password");
                configuration = new SecurityConfiguration(username, password);
            }
        }
        return configuration;
    }

    /**
     * Parses HTTP connection information and creates a connection configuration.
     *
     * @param definition the binding definition
     * @return the HTTP configuration
     * @throws InvalidConfigurationException if a configuration value is invalid
     */
    public static ConnectionConfiguration createConnectionConfiguration(WsBindingDefinition definition) throws InvalidConfigurationException {
        ConnectionConfiguration configuration = new ConnectionConfiguration();
        Map<String, String> configProperties = definition.getConfiguration();
        if (configProperties != null) {
            String connectTimeout = configProperties.get("connectTimeout");
            if (connectTimeout != null) {
                try {
                    configuration.setConnectTimeout(Integer.parseInt(connectTimeout));
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationException("Invalid connectTimeout", e);
                }
            }
            String requestTimeout = configProperties.get("requestTimeout");
            if (requestTimeout != null) {
                try {
                    configuration.setRequestTimeout(Integer.parseInt(requestTimeout));
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationException("Invalid requestTimeout", e);
                }
            }
            String clientStreamingChunkSize = configProperties.get("clientStreamingChunkSize");
            if (clientStreamingChunkSize != null) {
                try {
                    configuration.setClientStreamingChunkSize(Integer.parseInt(clientStreamingChunkSize));
                } catch (NumberFormatException e) {
                    throw new InvalidConfigurationException("Invalid clientStreamingChunkSize", e);
                }
            }
        }
        return configuration;
    }

    public static List<PhysicalBindingHandlerDefinition> generateBindingHandlers(URI domainUri, WsBindingDefinition definition) {
        List<PhysicalBindingHandlerDefinition> handlers = new ArrayList<PhysicalBindingHandlerDefinition>();
        for (BindingHandlerDefinition handlerDefinition : definition.getHandlers()) {
            // URIs specified in handler elements in a composite are relative and must be made absolute
            URI resolvedUri = URI.create(domainUri.toString() + "/" + handlerDefinition.getTarget());
            handlers.add(new PhysicalBindingHandlerDefinition(resolvedUri));
        }
        return handlers;
    }

    /**
     * Returns the WSDL name for an operation following JAX-WS rules. Namely, if present the <code>@WebMethod.operationName()</code> attribute value is used,
     * otherwise the default operation name is returned.
     *
     * @param operation    the operation definition
     * @param serviceClass the implementation class
     * @return the WSDL operation name
     */
    private static String getWsdlName(Operation operation, Class<?> serviceClass) {
        Method method = findMethod(operation, serviceClass);
        WebMethod annotation = method.getAnnotation(WebMethod.class);
        if (annotation == null || annotation.operationName().length() < 1) {
            return operation.getName();
        }
        return annotation.operationName();
    }

    /**
     * Returns a Method corresponding to the operation definition on a service implementation class.
     *
     * @param operation    the operation definition
     * @param serviceClass the implementation class
     * @return the method
     */
    @SuppressWarnings({"unchecked"})
    private static Method findMethod(Operation operation, Class<?> serviceClass) {
        List<DataType<?>> types = operation.getInputTypes();
        Class<?>[] params = new Class<?>[types.size()];
        for (int i = 0; i < types.size(); i++) {
            DataType<?> type = types.get(i);
            params[i] = type.getPhysical();
        }
        try {
            return serviceClass.getMethod(operation.getName(), params);
        } catch (NoSuchMethodException e) {
            // should not happen
            throw new AssertionError(e);
        }
    }

}
