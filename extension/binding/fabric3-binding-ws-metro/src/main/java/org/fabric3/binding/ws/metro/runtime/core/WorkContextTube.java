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
package org.fabric3.binding.ws.metro.runtime.core;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextCache;

/**
 * Populates invocation properties of an incoming request with a WorkContext. This is done so that the work context can be updated by other tubes, for
 * example, with an authenticated SecuritySubject.
 */
public class WorkContextTube extends AbstractFilterTubeImpl {

    public WorkContextTube(Tube next) {
        super(next);
    }

    private WorkContextTube(WorkContextTube original, TubeCloner cloner) {
        super(original, cloner);
    }

    public WorkContextTube copy(TubeCloner cloner) {
        return new WorkContextTube(this, cloner);
    }

    @Override
    public NextAction processRequest(Packet request) {
        WorkContext context = WorkContextCache.getAndResetThreadWorkContext();
        request.invocationProperties.put(MetroConstants.WORK_CONTEXT, context);
        return super.processRequest(request);
    }

}

