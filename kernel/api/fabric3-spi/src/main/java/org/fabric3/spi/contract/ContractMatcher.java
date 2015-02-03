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
 * Determines if two <code>ServiceContract</code>s are compatible for wiring. Specifically, tests whether the target contract can be converted to the
 * source contract type. Some interface languages, such as Java, allow for inheritance. In these cases, compatibility will include checking if a
 * widening conversion is possible from the target contract to source contract.
 *
 * This service delegates to {@link ContractMatcherExtension}s for particular mappings such as WSDL-to-Java.
 */
public interface ContractMatcher {

    /**
     * Determines if two <code>ServiceContract</code>s are compatible for wiring.
     *
     * @param source       the source contract. This is the contract specified by a component reference.
     * @param target       the target contract. This is the contract specified by a service.
     * @param reportErrors true if errors should be reported in the result
     * @return the match result
     */
    MatchResult isAssignableFrom(ServiceContract source, ServiceContract target, boolean reportErrors);

}