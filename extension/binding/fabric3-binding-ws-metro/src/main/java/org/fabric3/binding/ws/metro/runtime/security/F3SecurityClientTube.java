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
package org.fabric3.binding.ws.metro.runtime.security;

import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.assembler.ClientTubelineAssemblyContext;
import com.sun.xml.ws.security.policy.SecurityPolicyVersion;
import com.sun.xml.wss.SecurityEnvironment;
import com.sun.xml.wss.jaxws.impl.SecurityClientTube;

/**
 * Overrides the standard WSIT security tube behavior for the following:
 * <ul>
 * <li> Introduces a for workaround as described in {@link F3SecurityTubeFactory}.
 * <li> Overrides the default SecurityEnvironment with a Fabric3 implementation
 * </ul>
 */
public class F3SecurityClientTube extends SecurityClientTube {

    public F3SecurityClientTube(ClientTubelineAssemblyContext context, Tube nextTube) {
        super(context, nextTube);
        // override the default security environment with a Fabric3 system service
        secEnv = context.getContainer().getSPI(SecurityEnvironment.class);
    }

    protected F3SecurityClientTube(SecurityClientTube that, TubeCloner cloner) {
        super(that, cloner);
    }

    @Override
    protected void collectPolicies() {
        // workaround for NPE when policies only attached at the WSDL operation level
        spVersion = SecurityPolicyVersion.SECURITYPOLICY12NS;
        super.collectPolicies();
    }
}
