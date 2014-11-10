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
 */
package org.fabric3.binding.ws.metro.runtime.core;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;

import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;

/**
 * Populates invocation properties of an incoming request with a WorkContext. This is done so that the work context can be updated by other tubes, for example,
 * with an authenticated SecuritySubject.
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
        try {
            request.invocationProperties.put(MetroConstants.WORK_CONTEXT, context);
            return super.processRequest(request);
        } finally {
            context.reset();
        }
    }

}

