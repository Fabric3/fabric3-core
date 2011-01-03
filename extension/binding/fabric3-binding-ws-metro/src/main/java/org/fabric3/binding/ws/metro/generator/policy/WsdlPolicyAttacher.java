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
*/
package org.fabric3.binding.ws.metro.generator.policy;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.metro.generator.PolicyExpressionMapping;


/**
 * Inlines WS-Policy expressions in a WSDL document. Specifically, PolicyReference elements are added to WSDL subjects.
 *
 * @version $Rev$ $Date$
 */
public interface WsdlPolicyAttacher {

    /**
     * Attaches endpoint and operation policy expressions to a WSDL document. Endpoint policies will be attached to the binding while operation
     * policies will be attached to the port type operation element.
     *
     * @param wsdl             the WSDL document
     * @param endpointPolicies the endpoint policies
     * @param mappings         a mapping of policy expressions to operations. Operation overloading is not supported.
     * @throws PolicyAttachmentException if an attachment error is encountered
     */
    void attach(Document wsdl, List<Element> endpointPolicies, List<PolicyExpressionMapping> mappings) throws PolicyAttachmentException;

}