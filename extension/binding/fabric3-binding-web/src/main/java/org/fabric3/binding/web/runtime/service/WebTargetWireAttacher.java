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
package org.fabric3.binding.web.runtime.service;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.web.provision.WebTargetDefinition;
import org.fabric3.binding.web.runtime.common.BroadcasterManager;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches a callback proxy to a websocket.
 *
 * @version $Rev: 9436 $ $Date: 2010-09-10 17:13:50 +0200 (Fri, 10 Sep 2010) $
 */
@EagerInit
public class WebTargetWireAttacher implements TargetWireAttacher<WebTargetDefinition> {
    private BroadcasterManager broadcasterManager;

    public WebTargetWireAttacher(@Reference BroadcasterManager broadcasterManager) {
        this.broadcasterManager = broadcasterManager;
    }

    public void attach(PhysicalSourceDefinition source, WebTargetDefinition target, Wire wire) throws WiringException {
        WebCallbackInterceptor interceptor = new WebCallbackInterceptor(broadcasterManager);
        for (InvocationChain chain : wire.getInvocationChains()) {
            chain.addInterceptor(interceptor);
        }
    }

    public void detach(PhysicalSourceDefinition source, WebTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(WebTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }
}
