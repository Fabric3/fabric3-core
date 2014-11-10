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
package org.fabric3.spi.contract;

import org.fabric3.api.model.type.contract.ServiceContract;

/**
 * Determines if <code>ServiceContract</code>s are compatible using a particular mapping, for example, WSDL-to-Java.
 */
public interface ContractMatcherExtension<S extends ServiceContract, T extends ServiceContract> {

    /**
     * Returns the contract type this extension maps from
     *
     * @return the contract type this extension maps from
     */
    Class<S> getSource();

    /**
     * Returns the contract type this extension maps to
     *
     * @return the contract type this extension maps to
     */
    Class<T> getTarget();

    /**
     * Determines if two <code>ServiceContract</code>s are compatible for wiring. Some interface languages, such as Java, allow for inheritance. In these cases,
     * compatibility will include checking if the source contract is a super type of the target contract.
     *
     * @param source       the source contract. Typically, this is the contract specified by a component reference.
     * @param target       the target contract. Typically this is the contract specified by a service.
     * @param reportErrors true if errors should be reported in the result
     * @return the result
     */
    MatchResult isAssignableFrom(S source, T target, boolean reportErrors);
}
