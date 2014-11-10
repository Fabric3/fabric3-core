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
package org.fabric3.spi.introspection.java.policy;

import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Introspects service operations for policy annotations on a component implementation class. The service operations are first mapped to corresponding
 * methods on the implementation class and then processed. This service must be called after the service contract(s) for the implementation class are
 * determined (either explicitly using @Service or heuristically) as opposed to when the implementation class is first introspected so that the
 * service operations can be updated with policy metadata.
 */
public interface OperationPolicyIntrospector {

    /**
     * Maps service operations to implementation class methods and then processes policy metadata annotated on those methods.
     *
     * @param contract  the service contract containing operations to process
     * @param implClass the component implementation class
     * @param context   the current introspection context
     */
    void introspectPolicyOnOperations(ServiceContract contract, Class<?> implClass, IntrospectionContext context);

}
