/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.contribution.archive;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;

/**
 * The base class for ContributionProcessor implementations
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ContributionProcessor.class)
public abstract class AbstractContributionProcessor implements ContributionProcessor {
    protected ProcessorRegistry registry;

    /**
     * Sets the ContributionProcessorRegistry that this processor should register with/
     *
     * @param registry the ContributionProcessorRegistry that this processor should register with
     */
    @Reference
    public void setContributionProcessorRegistry(ProcessorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Initialize the processor and registers with the processor registry.
     */
    @Init
    public void start() {
        registry.register(this);
    }

    /**
     * Shuts the processor down and unregisters from the processor registry.
     */
    @Destroy
    public void stop() {
        registry.unregisterContributionProcessor(this);
    }

}
