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
package org.fabric3.fabric.instantiator;

import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Implementation that resolves promotions using PromotionResolutionService and delegates to TargetResolutionServices to resolve reference targets.
 *
 * @version $Rev$ $Date$
 */
public class ResolutionServiceImpl implements ResolutionService {

    private PromotionResolutionService promotionResolutionService;
    private TargetResolutionService explicitResolutionService;
    private TargetResolutionService autowireResolutionService;


    public ResolutionServiceImpl(@Reference(name = "promotionResolutionService") PromotionResolutionService promotionResolutionService,
                                 @Reference(name = "explicitResolutionService") TargetResolutionService explicitResolutionService,
                                 @Reference(name = "autowireResolutionService") TargetResolutionService autowireResolutionService) {
        this.promotionResolutionService = promotionResolutionService;
        this.explicitResolutionService = explicitResolutionService;
        this.autowireResolutionService = autowireResolutionService;
    }

    public void resolve(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        resolveReferences(logicalComponent, context);
        resolveServices(logicalComponent, context);
        if (logicalComponent instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent compositeComponent = (LogicalCompositeComponent) logicalComponent;
            for (LogicalComponent<?> child : compositeComponent.getComponents()) {
                resolve(child, context);
            }
        }
    }

    public void resolve(LogicalService logicalService, InstantiationContext context) {
        promotionResolutionService.resolve(logicalService, context);
    }

    public void resolve(LogicalReference reference, LogicalCompositeComponent component, InstantiationContext context) {
        promotionResolutionService.resolve(reference, context);
        explicitResolutionService.resolve(reference, component, context);
        if (reference.isResolved()) {
            return;
        }
        autowireResolutionService.resolve(reference, component, context);
    }

    /*
     * Handles promotions and target resolution on references.
     */
    private void resolveReferences(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        LogicalCompositeComponent parent = logicalComponent.getParent();
        for (LogicalReference reference : logicalComponent.getReferences()) {
            Multiplicity multiplicityValue = reference.getDefinition().getMultiplicity();
            boolean refMultiplicity = multiplicityValue.equals(Multiplicity.ZERO_N) || multiplicityValue.equals(Multiplicity.ONE_N);
            if (refMultiplicity || !reference.isResolved()) {
                // Only resolve references that have not been resolved or ones that are multiplicities since the latter may be reinjected.
                // Explicitly set the reference to unresolved, since if it was a multiplicity it may have been previously resolved.
                reference.setResolved(false);
                promotionResolutionService.resolve(reference, context);
                explicitResolutionService.resolve(reference, parent, context);
                if (reference.isResolved()) {
                    continue;
                }
                autowireResolutionService.resolve(reference, parent, context);
            }
        }
    }

    /*
     * Handles promotions on services.
     */
    private void resolveServices(LogicalComponent<?> logicalComponent, InstantiationContext context) {
        for (LogicalService logicalService : logicalComponent.getServices()) {
            promotionResolutionService.resolve(logicalService, context);
        }
    }

}
