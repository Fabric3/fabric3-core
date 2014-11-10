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

import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.assembler.ServerTubelineAssemblyContext;
import com.sun.xml.ws.assembler.TubeFactory;

/**
 *
 */
public class WorkContextTubeFactory implements TubeFactory {
    public Tube createTube(ClientTubelineAssemblyContext context) throws WebServiceException {
        throw new UnsupportedOperationException("Tube not supported in client tublines");
    }

    public Tube createTube(ServerTubelineAssemblyContext context) throws WebServiceException {
        return new WorkContextTube(context.getTubelineHead());
    }

}
