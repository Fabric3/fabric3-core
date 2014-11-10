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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.policy.xpath;

import java.util.Collections;
import java.util.List;

import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 * Implements the URIRef function defined by the SCA Policy Specification.
 */
public class UriRefFunction implements Function {

    @SuppressWarnings({"unchecked"})
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() != 1) {
            throw new FunctionCallException("Invalid number of arguments for URIRef(): " + args.size());
        }
        String uri = args.get(0).toString();
        List<LogicalComponent<?>> nodeSet = context.getNodeSet();
        for (LogicalComponent<?> component : nodeSet) {
            if (component.getUri().getSchemeSpecificPart().equals(uri)) {
                return component;
            } else if (component instanceof LogicalCompositeComponent) {
                LogicalComponent ret = find(uri, (LogicalCompositeComponent) component);
                if (ret != null) {
                    return ret;
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Recurses the composite hierarchy for the component with the given URI
     *
     * @param uri       the uri
     * @param composite the composite to recurse
     * @return the component or null if not found
     */
    private LogicalComponent find(String uri, LogicalCompositeComponent composite) {
        for (LogicalComponent child : composite.getComponents()) {
            if (child.getUri().getSchemeSpecificPart().equals(uri)) {
                return child;
            }
            if (child instanceof LogicalCompositeComponent) {
                LogicalComponent component = find(uri, (LogicalCompositeComponent) child);
                if (component != null) {
                    return component;
                }
            }
        }
        return null;

    }
}
