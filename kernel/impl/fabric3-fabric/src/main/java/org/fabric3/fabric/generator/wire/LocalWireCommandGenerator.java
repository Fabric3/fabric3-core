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
package org.fabric3.fabric.generator.wire;

import java.net.URI;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.command.AttachWireCommand;
import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.fabric.command.DetachWireCommand;
import org.fabric3.host.Names;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.util.UriHelper;

/**
 * Generate commands to attach local wires between components.
 *
 * @version $Rev$ $Date$
 */
public class LocalWireCommandGenerator implements CommandGenerator {

    private WireGenerator wireGenerator;
    private LogicalComponentManager applicationLCM;
    private LogicalComponentManager runtimeLCM;
    private int order;

    /**
     * Constructor used during bootstrap.
     *
     * @param wireGenerator the bootstrap physical wire generator
     * @param runtimeLCM    the bootstrap LogicalComponentManager
     * @param order         the order value for commands generated
     */
    public LocalWireCommandGenerator(WireGenerator wireGenerator, LogicalComponentManager runtimeLCM, int order) {
        this.wireGenerator = wireGenerator;
        this.runtimeLCM = runtimeLCM;
        this.order = order;
    }

    /**
     * Constructor used for instantiation after bootstrap. After bootstrap on a controller instance, two domains will be active: the runtime domain
     * containing system components and the application domain containing end-user components. On runtime nodes, the application domain may not be
     * active, in which case a null value may be injected.
     *
     * @param wireGenerator  the bootstrap physical wire generator
     * @param runtimeLCM     the LogicalComponentManager associated with the runtime domain
     * @param applicationLCM the LogicalComponentManager associated with the application domain
     * @param order          the order value for commands generated
     */
    @Constructor
    public LocalWireCommandGenerator(@Reference WireGenerator wireGenerator,
                                     @Reference(name = "runtimeLCM") LogicalComponentManager runtimeLCM,
                                     @Reference(name = "applicationLCM") LogicalComponentManager applicationLCM,
                                     @Property(name = "order") int order) {
        this.wireGenerator = wireGenerator;
        this.runtimeLCM = runtimeLCM;
        this.applicationLCM = applicationLCM;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public ConnectionCommand generate(LogicalComponent<?> component, boolean incremental) throws GenerationException {
        if (component instanceof LogicalCompositeComponent || LogicalState.MARKED == component.getState()) {
            return null;
        }
        ConnectionCommand command = new ConnectionCommand();

        for (LogicalReference logicalReference : component.getReferences()) {
            if (logicalReference.getBindings().isEmpty()) {
                generateUnboundReferenceWires(logicalReference, command, incremental);
            }
        }
        if (command.getAttachCommands().isEmpty() && command.getDetachCommands().isEmpty()) {
            return null;
        }
        return command;
    }

    private void generateUnboundReferenceWires(LogicalReference logicalReference, ConnectionCommand command, boolean incremental)
            throws GenerationException {

        // if the reference is a multiplicity and one of the wires has changed, all of the wires need to be regenerated for reinjection
        boolean reinjection = isReinjection(logicalReference, incremental);

        for (LogicalWire logicalWire : logicalReference.getWires()) {
            URI uri = logicalWire.getTargetUri();
            LogicalComponent<?> target = findTarget(logicalWire);
            if (!reinjection && (logicalWire.getState() == LogicalState.PROVISIONED && target.getState() != LogicalState.MARKED && incremental)) {
                continue;
            }
            String serviceName = uri.getFragment();
            LogicalService targetService = target.getService(serviceName);
            if (targetService == null) {
                throw new AssertionError("Target service not found: " + uri);
            }
            while (CompositeImplementation.class.isInstance(target.getDefinition().getImplementation())) {
                LogicalCompositeComponent composite = (LogicalCompositeComponent) target;
                URI promoteUri = targetService.getPromotedUri();
                URI promotedComponent = UriHelper.getDefragmentedName(promoteUri);
                target = composite.getComponent(promotedComponent);
                targetService = target.getService(promoteUri.getFragment());
            }

            LogicalReference reference = logicalWire.getSource();
            boolean attach = true;
            if (target.getState() == LogicalState.MARKED || logicalWire.getState() == LogicalState.MARKED) {
                PhysicalWireDefinition pwd = wireGenerator.generateCollocatedWire(reference, targetService);
                attach = false;
                DetachWireCommand detachCommand = new DetachWireCommand();
                detachCommand.setPhysicalWireDefinition(pwd);
                command.add(detachCommand);
            } else if (reinjection || !incremental || logicalWire.getState() == LogicalState.NEW || target.getState() == LogicalState.NEW) {
                PhysicalWireDefinition pwd = wireGenerator.generateCollocatedWire(reference, targetService);
                AttachWireCommand attachCommand = new AttachWireCommand();
                attachCommand.setPhysicalWireDefinition(pwd);
                command.add(attachCommand);
            }
            // generate physical callback wires if the forward service is bidirectional
            if (reference.getDefinition().getServiceContract().getCallbackContract() != null) {
                PhysicalWireDefinition pwd = wireGenerator.generateCollocatedCallbackWire(targetService, reference);
                if (attach) {
                    AttachWireCommand attachCommand = new AttachWireCommand();
                    attachCommand.setPhysicalWireDefinition(pwd);
                    command.add(attachCommand);
                } else {
                    DetachWireCommand detachCommand = new DetachWireCommand();
                    detachCommand.setPhysicalWireDefinition(pwd);
                    command.add(detachCommand);
                }
            }

        }

    }

    private boolean isReinjection(LogicalReference logicalReference, boolean incremental) {
        Multiplicity multiplicity = logicalReference.getDefinition().getMultiplicity();
        if (incremental && multiplicity == Multiplicity.ZERO_N || multiplicity == Multiplicity.ONE_N) {
            for (LogicalWire wire : logicalReference.getWires()) {
                LogicalComponent<?> target = findTarget(wire);
                // check the source and target sides since a target may have been added or removed
                if (wire.getState() == LogicalState.NEW
                        || wire.getState() == LogicalState.MARKED
                        || target.getState() == LogicalState.NEW
                        || target.getState() == LogicalState.MARKED) {
                    return true;
                }
            }
        }
        return false;
    }

    private LogicalComponent<?> findTarget(LogicalWire logicalWire) {
        URI uri = UriHelper.getDefragmentedName(logicalWire.getTargetUri());
        if (uri.toString().startsWith(Names.RUNTIME_NAME)) {
            return runtimeLCM.getComponent(uri);
        } else {
            return applicationLCM.getComponent(uri);
        }
    }

}