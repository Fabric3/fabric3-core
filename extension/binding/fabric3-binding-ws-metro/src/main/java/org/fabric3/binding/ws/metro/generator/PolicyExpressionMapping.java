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
package org.fabric3.binding.ws.metro.generator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Maps policy expressions to the WSDL operations they are attached to. Note that operation names may not correspond to a Java method name on the
 * service implementation class as the WSDL operation name may be specified using the <code>@WebMethod.operationName()</code> attribute. In this case,
 * the WSDL operation name will correspond to the attribute value and not the method name.
 * <p/>
 * Operation overloading is not supported.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class PolicyExpressionMapping implements Serializable {
    private static final long serialVersionUID = 5554492976250872672L;

    private String id;
    private Element policyExpression;
    private List<String> operations = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param id               the policy expression id provided as an attribute on the Policy element as defined by the WS-Security utility
     *                         extensions.
     * @param policyExpression the WS-Policy policy expression rendered as a DOM
     */
    public PolicyExpressionMapping(String id, Element policyExpression) {
        this.id = id;
        this.policyExpression = policyExpression;
    }

    public String getId() {
        return id;
    }

    public Element getPolicyExpression() {
        return policyExpression;
    }

    public void addOperationName(String name) {
        operations.add(name);
    }

    public List<String> getOperationNames() {
        return operations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PolicyExpressionMapping that = (PolicyExpressionMapping) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (policyExpression != null ? policyExpression.hashCode() : 0);
        result = 31 * result + (operations != null ? operations.hashCode() : 0);
        return result;
    }
}
