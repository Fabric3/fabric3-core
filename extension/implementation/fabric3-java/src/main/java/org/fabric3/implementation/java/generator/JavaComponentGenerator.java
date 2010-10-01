/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.implementation.java.model.JavaImplementation;
import org.fabric3.implementation.java.provision.JavaComponentDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionSourceDefinition;
import org.fabric3.implementation.java.provision.JavaConnectionTargetDefinition;
import org.fabric3.implementation.java.provision.JavaSourceDefinition;
import org.fabric3.implementation.java.provision.JavaTargetDefinition;
import org.fabric3.implementation.pojo.generator.GenerationHelper;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.generator.policy.EffectivePolicy;

/**
 * Generates physical metadata for a Java component deployment.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaComponentGenerator implements ComponentGenerator<LogicalComponent<JavaImplementation>> {
    protected final GenerationHelper ifHelper;
    private JavaGenerationHelper generationHelper;

    public JavaComponentGenerator(@Reference JavaGenerationHelper generationHelper, @Reference GenerationHelper ifHelper) {
        this.generationHelper = generationHelper;
        this.ifHelper = ifHelper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<JavaImplementation> component) throws GenerationException {
        JavaComponentDefinition definition = new JavaComponentDefinition();
        generationHelper.generate(definition, component);
        return definition;
    }

    public PhysicalSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        JavaSourceDefinition definition = new JavaSourceDefinition();
        generationHelper.generateWireSource(definition, reference, policy);
        return definition;
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        JavaSourceDefinition definition = new JavaSourceDefinition();
        ServiceContract callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
        LogicalComponent<JavaImplementation> source = (LogicalComponent<JavaImplementation>) service.getLeafComponent();
        generationHelper.generateCallbackWireSource(definition, source, callbackContract, policy);
        return definition;
    }

    public PhysicalTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        JavaTargetDefinition definition = new JavaTargetDefinition();
        generationHelper.generateWireTarget(definition, service);
        return definition;
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws GenerationException {
        JavaConnectionSourceDefinition definition = new JavaConnectionSourceDefinition();
        generationHelper.generateConnectionSource(definition, producer);
        return definition;
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        JavaConnectionTargetDefinition definition = new JavaConnectionTargetDefinition();
        generationHelper.generateConnectionTarget(definition, consumer);
        return definition;
    }

    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        JavaSourceDefinition definition = new JavaSourceDefinition();
        generationHelper.generateResourceWireSource(definition, resourceReference);
        return definition;
    }

}
