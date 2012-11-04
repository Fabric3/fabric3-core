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
package org.fabric3.binding.ws.metro.generator.policy;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.fabric3.binding.ws.metro.generator.PolicyExpressionMapping;

/**
 * Default implementation of WsdlPolicyAttacher. Policy expression attachment is performed by cloning the policy expression element and appending it
 * to the list of operations.
 */
public class WsdlPolicyAttacherImpl implements WsdlPolicyAttacher {
    private static final String WS_POLICY_NS = "http://www.w3.org/ns/ws-policy";
    private static final String WS_SECURITY_UTILITY_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";

    public void attach(Document wsdl, List<Element> endpointPolicies, List<PolicyExpressionMapping> mappings) throws PolicyAttachmentException {
        attachEndpointPolicies(wsdl, endpointPolicies);
        attachOperationPolicies(wsdl, mappings);
    }

    private void attachEndpointPolicies(Document document, List<Element> endpointPolicies) throws PolicyAttachmentException {
        Element root = document.getDocumentElement();
        for (Element policy : endpointPolicies) {
            Node clone = policy.cloneNode(true);
            document.adoptNode(clone);
            root.appendChild(clone);

            // calculate the policy id to use in the PolicyReference element
            Node item = clone.getAttributes().getNamedItemNS(WS_SECURITY_UTILITY_NS, "Id");
            if (item == null) {
                throw new PolicyAttachmentException("Missing id in policy expression");
            }
            String id = "#" + item.getNodeValue();
            // add the policy id to the wsdl binding
            Node bindingNode = findChildNode(root, "binding");
            if (bindingNode == null) {
                throw new PolicyAttachmentException("Binding element missing");
            }

            // attach the reference to the binding node
            Element reference = bindingNode.getOwnerDocument().createElementNS(WS_POLICY_NS, "PolicyReference");
            reference.setAttribute("URI", id);
            bindingNode.appendChild(reference);
        }
    }

    private void attachOperationPolicies(Document document, List<PolicyExpressionMapping> mappings) throws PolicyAttachmentException {
        Element root = document.getDocumentElement();
        for (PolicyExpressionMapping mapping : mappings) {
            Element policy = mapping.getPolicyExpression();
            List<String> operationNames = mapping.getOperationNames();
            Node clone = policy.cloneNode(true);
            document.adoptNode(clone);
            root.appendChild(clone);

            // calculate the policy id to use in the PolicyReference element
            Node item = clone.getAttributes().getNamedItemNS(WS_SECURITY_UTILITY_NS, "Id");
            if (item == null) {
                throw new PolicyAttachmentException("Missing id in policy expression");
            }
            String id = "#" + item.getNodeValue();

            // add the policy id to the wsdl binding
            Node portTypeNode = findChildNode(root, "portType");
            if (portTypeNode == null) {
                throw new PolicyAttachmentException("Port type element missing");
            }

            addPolicyReferenceToOperation(portTypeNode, operationNames, id);
        }
    }

    /**
     * Adds a policy reference to the WSDL operation definition using the WS-Policy PolicyReference element.
     *
     * @param node           the operation node
     * @param operationNames the list of operations the policy applies to
     * @param id             the policy reference id, e.g. "#SomePolicy"
     */
    private void addPolicyReferenceToOperation(Node node, List<String> operationNames, String id) {
        for (Node childNode = node.getFirstChild(); childNode != null;) {
            Node nextChild = childNode.getNextSibling();
            if (childNode.getNodeName().equals("operation")) {
                if (operationNames.contains(childNode.getAttributes().getNamedItem("name").getNodeValue())) {
                    Element reference = node.getOwnerDocument().createElementNS(WS_POLICY_NS, "PolicyReference");
                    reference.setAttribute("URI", id);
                    childNode.appendChild(reference);
                }
            }
            childNode = nextChild;
        }
    }

    /**
     * Finds a child node.
     *
     * @param parent the parent node
     * @param name   the child node name
     * @return the node or null if not found
     */
    private Node findChildNode(Node parent, String name) {
        for (Node childNode = parent.getFirstChild(); childNode != null;) {
            Node nextChild = childNode.getNextSibling();
            if (childNode.getNodeName().equals(name)) {
                return childNode;
            }
            childNode = nextChild;
        }
        return null;
    }
}