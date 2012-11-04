/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.monitor.generator;

import java.net.URI;

import org.oasisopen.sca.annotation.EagerInit;
import org.w3c.dom.Element;

import org.fabric3.monitor.model.MonitorImplementation;
import org.fabric3.monitor.provision.MonitorComponentDefinition;
import org.fabric3.monitor.provision.MonitorConnectionTargetDefinition;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
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

/**
 *
 */
@EagerInit
public class MonitorComponentGenerator implements ComponentGenerator<LogicalComponent<MonitorImplementation>> {

    public PhysicalComponentDefinition generate(LogicalComponent<MonitorImplementation> component) throws GenerationException {
        MonitorImplementation implementation = component.getDefinition().getImplementation();
        Element configuration = implementation.getConfiguration();
        return new MonitorComponentDefinition(configuration);
    }

    @SuppressWarnings({"unchecked"})
    public PhysicalConnectionTargetDefinition generateConnectionTarget(LogicalConsumer consumer) throws GenerationException {
        LogicalComponent<? extends MonitorImplementation> component = (LogicalComponent<? extends MonitorImplementation>) consumer.getParent();
        URI uri = component.getUri();
        return new MonitorConnectionTargetDefinition(uri);
    }

    public PhysicalSourceDefinition generateSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalTargetDefinition generateTarget(LogicalService service, EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalSourceDefinition generateCallbackSource(LogicalService service, EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionSourceDefinition generateConnectionSource(LogicalProducer producer) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalSourceDefinition generateResourceSource(LogicalResourceReference<?> resourceReference) throws GenerationException {
        throw new UnsupportedOperationException();
    }
}