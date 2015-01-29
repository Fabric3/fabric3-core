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
package org.fabric3.implementation.java.generator;

import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionTargetDefinition;
import org.fabric3.implementation.java.provision.JavaWireSourceDefinition;
import org.fabric3.implementation.java.provision.JavaWireTargetDefinition;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Handles generation for Java components and specialized subtypes.
 */
public interface JavaGenerationHelper {

    /**
     * Populates the JavaComponentDefinition with generation information
     *
     * @param definition the JavaComponentDefinition to populate
     * @param component  the component being generated
     * @throws GenerationException if there is an error generating the JavaComponentDefinition
     */
    void generate(JavaComponentDefinition definition, LogicalComponent<? extends JavaImplementation> component) throws GenerationException;

    /**
     * Populates the JavaWireSourceDefinition with reference wiring information.
     *
     * @param definition the JavaWireSourceDefinition to populate
     * @param reference  the reference the wire is being generated for
     * @throws GenerationException if there is an error generating the JavaWireSourceDefinition
     */
    void generateWireSource(JavaWireSourceDefinition definition, LogicalReference reference) throws GenerationException;

    /**
     * Populates the JavaWireSourceDefinition with callback wiring information.
     *
     * @param definition      the JavaWireSourceDefinition to populate
     * @param component       the component to be injected with the callback, i.e. the component providing the forward service
     * @param serviceContract the callback service contract
     * @throws GenerationException if there is an error generating the JavaWireSourceDefinition
     */
    void generateCallbackWireSource(JavaWireSourceDefinition definition,
                                    LogicalComponent<? extends JavaImplementation> component,
                                    ServiceContract serviceContract) throws GenerationException;

    /**
     * Populates the JavaWireTargetDefinition with wiring information.
     *
     * @param definition the JavaWireTargetDefinition to populate
     * @param service    the target service for the wire
     * @throws GenerationException if there is an error generating the JavaWireSourceDefinition
     */
    void generateWireTarget(JavaWireTargetDefinition definition, LogicalService service) throws GenerationException;

    /**
     * Populates the JavaConnectionSourceDefinition with information for connecting it to a component producer.
     *
     * @param definition the JavaConnectionSourceDefinition to populate
     * @param producer   the producer
     * @throws GenerationException if there is an error generating the JavaConnectionSourceDefinition
     */
    void generateConnectionSource(JavaConnectionSourceDefinition definition, LogicalProducer producer) throws GenerationException;

    /**
     * Populates the JavaConnectionTargetDefinition with information for connecting it to a component consumer .
     *
     * @param definition the JavaConnectionTargetDefinition to populate
     * @param consumer   the consumer
     * @throws GenerationException if there is an error generating the JavaConnectionSourceDefinition
     */
    void generateConnectionTarget(JavaConnectionTargetDefinition definition, LogicalConsumer consumer) throws GenerationException;

    /**
     * Populates the JavaWireSourceDefinition with resource wiring information.
     *
     * @param definition        the JavaWireSourceDefinition to populate
     * @param resourceReference the resource to be wired
     * @throws GenerationException if there is an error generating the JavaWireSourceDefinition
     */
    void generateResourceWireSource(JavaWireSourceDefinition definition, LogicalResourceReference<?> resourceReference) throws GenerationException;

}
