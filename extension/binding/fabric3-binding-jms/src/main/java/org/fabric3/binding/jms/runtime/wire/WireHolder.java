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
package org.fabric3.binding.jms.runtime.wire;

import java.util.List;

import org.fabric3.api.binding.jms.model.CorrelationScheme;

/**
 * Holder for Wires and required metadata for performing an invocation.
 */
public class WireHolder {
    private List<InvocationChainHolder> chains;
    private CorrelationScheme correlationScheme;

    /**
     * Constructor.
     *
     * @param chains            InvocationChains contained by the wire
     * @param correlationScheme the correlation scheme if the wire uses request-response, otherwise null
     */
    public WireHolder(List<InvocationChainHolder> chains, CorrelationScheme correlationScheme) {
        this.chains = chains;
        this.correlationScheme = correlationScheme;
    }

    public CorrelationScheme getCorrelationScheme() {
        return correlationScheme;
    }

    public List<InvocationChainHolder> getInvocationChains() {
        return chains;
    }

}