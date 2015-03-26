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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.provision.JavaConnectionSource;
import org.fabric3.implementation.java.provision.JavaConnectionTarget;
import org.fabric3.implementation.java.provision.PhysicalJavaComponent;
import org.fabric3.implementation.java.provision.JavaWireSource;
import org.fabric3.implementation.java.provision.JavaWireTarget;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * Handles generation for Java components and specialized subtypes.
 */
public interface JavaGenerationHelper {

    /**
     * Populates the JavaComponentDefinition with generation information
     *
     * @param physicalComponent the JavaComponentDefinition to populate
     * @param component         the component being generated
     * @throws Fabric3Exception if there is an error generating the JavaComponentDefinition
     */
    void generate(PhysicalJavaComponent physicalComponent, LogicalComponent<? extends JavaImplementation> component) throws Fabric3Exception;

    /**
     * Populates the source with reference wiring information.
     *
     * @param source    the source to populate
     * @param reference the reference the wire is being generated for
     * @throws Fabric3Exception if there is an error generating the source
     */
    void generateWireSource(JavaWireSource source, LogicalReference reference) throws Fabric3Exception;

    /**
     * Populates the source with callback wiring information.
     *
     * @param source    the source to populate
     * @param component the component to be injected with the callback, i.e. the component providing the forward service
     * @param contract  the callback service contract
     * @throws Fabric3Exception if there is an error generating the source
     */
    void generateCallbackWireSource(JavaWireSource source, LogicalComponent<? extends JavaImplementation> component, JavaServiceContract contract)
            throws Fabric3Exception;

    /**
     * Populates the JavaWireTargetDefinition with wiring information.
     *
     * @param target  the JavaWireTargetDefinition to populate
     * @param service the target service for the wire
     * @throws Fabric3Exception if there is an error generating the source
     */
    void generateWireTarget(JavaWireTarget target, LogicalService service) throws Fabric3Exception;

    /**
     * Populates the JavaConnectionSourceDefinition with information for connecting it to a component producer.
     *
     * @param source   the JavaConnectionSourceDefinition to populate
     * @param producer the producer
     * @throws Fabric3Exception if there is an error generating the JavaConnectionSourceDefinition
     */
    void generateConnectionSource(JavaConnectionSource source, LogicalProducer producer) throws Fabric3Exception;

    /**
     * Populates the JavaConnectionTargetDefinition with information for connecting it to a component consumer .
     *
     * @param target   the JavaConnectionTargetDefinition to populate
     * @param consumer the consumer
     * @throws Fabric3Exception if there is an error generating the JavaConnectionSourceDefinition
     */
    void generateConnectionTarget(JavaConnectionTarget target, LogicalConsumer consumer) throws Fabric3Exception;

    /**
     * Populates the source with resource wiring information.
     *
     * @param source            the source to populate
     * @param resourceReference the resource to be wired
     * @throws Fabric3Exception if there is an error generating the source
     */
    void generateResourceWireSource(JavaWireSource source, LogicalResourceReference<?> resourceReference) throws Fabric3Exception;

}
