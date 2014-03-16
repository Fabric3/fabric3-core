/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.implementation.java.generator;

import org.fabric3.api.model.type.java.JavaImplementation;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionTargetDefinition;
import org.fabric3.implementation.java.provision.JavaWireSourceDefinition;
import org.fabric3.implementation.java.provision.JavaWireTargetDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
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
     * @param policy     the effective wire policy
     * @throws GenerationException if there is an error generating the JavaWireSourceDefinition
     */
    void generateWireSource(JavaWireSourceDefinition definition, LogicalReference reference, EffectivePolicy policy) throws GenerationException;

    /**
     * Populates the JavaWireSourceDefinition with callback wiring information.
     *
     * @param definition      the JavaWireSourceDefinition to populate
     * @param component       the component to be injected with the callback, i.e. the component providing the forward service
     * @param serviceContract the callback service contract
     * @param policy          the effective wire policy
     * @throws GenerationException if there is an error generating the JavaWireSourceDefinition
     */
    void generateCallbackWireSource(JavaWireSourceDefinition definition,
                                    LogicalComponent<? extends JavaImplementation> component,
                                    ServiceContract serviceContract,
                                    EffectivePolicy policy) throws GenerationException;

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
     * @param definition the JavaWireSourceDefinition to populate
     * @param resourceReference   the resource to be wired
     * @throws GenerationException if there is an error generating the JavaWireSourceDefinition
     */
    void generateResourceWireSource(JavaWireSourceDefinition definition, LogicalResourceReference<?> resourceReference) throws GenerationException;

}
