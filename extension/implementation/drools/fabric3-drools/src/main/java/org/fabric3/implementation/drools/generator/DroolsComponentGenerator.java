package org.fabric3.implementation.drools.generator;

import org.fabric3.implementation.drools.model.DroolsImplementation;
import org.fabric3.implementation.drools.provision.DroolsComponentDefinition;
import org.fabric3.implementation.drools.provision.DroolsSourceDefinition;
import org.fabric3.implementation.drools.provision.DroolsTargetDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalConnectionSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.type.java.InjectableType;

public class DroolsComponentGenerator implements ComponentGenerator<LogicalComponent<DroolsImplementation>> {

    public DroolsComponentDefinition generate(LogicalComponent<DroolsImplementation> component) throws GenerationException {
        DroolsImplementation implementation = component.getDefinition().getImplementation();
        return new DroolsComponentDefinition(implementation.getPackages());
    }

    public DroolsSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        ServiceContract serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        if (reference.getDefinition().isKeyed()) {
            String keyClass = reference.getDefinition().getKeyDataType().getPhysical().getName();
            return new DroolsSourceDefinition(reference.getDefinition().getName(), interfaceName, InjectableType.REFERENCE, keyClass);
        }
        return  new DroolsSourceDefinition(reference.getDefinition().getName(), interfaceName, InjectableType.REFERENCE);
    }

    public DroolsTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        return new DroolsTargetDefinition();
    }

    public DroolsSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        ServiceContract callbackContract = service.getDefinition().getServiceContract().getCallbackContract();
        String interfaceName = callbackContract.getQualifiedInterfaceName();
        return new DroolsSourceDefinition(service.getDefinition().getName(), interfaceName, InjectableType.CALLBACK);
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        throw new UnsupportedOperationException();
    }
}
