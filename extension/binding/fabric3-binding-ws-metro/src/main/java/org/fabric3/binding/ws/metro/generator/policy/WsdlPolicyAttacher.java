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
package org.fabric3.binding.ws.metro.generator.policy;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.metro.generator.PolicyExpressionMapping;


/**
 * Inlines WS-Policy expressions in a WSDL document. Specifically, PolicyReference elements are added to WSDL subjects.
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