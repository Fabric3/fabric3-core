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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.junit.runtime;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.junit.common.ContextConfiguration;
import org.fabric3.implementation.junit.provision.JUnitWireSource;
import org.fabric3.implementation.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.spi.container.builder.SourceWireAttacher;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.test.spi.TestWireHolder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class JunitSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<JUnitWireSource> {
    private TestWireHolder holder;
    private AuthenticationService authenticationService;

    public JunitSourceWireAttacher(@Reference TransformerRegistry transformerRegistry, @Reference TestWireHolder holder) {
        super(transformerRegistry);
        this.holder = holder;
    }

    @Reference(required = false)
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void attach(JUnitWireSource source, PhysicalWireTarget target, Wire wire) {
        String testName = source.getTestName();
        ContextConfiguration configuration = source.getConfiguration();
        if (configuration != null) {
            if (authenticationService == null) {
                throw new Fabric3Exception("Security information set for the test but a security extension has not been installed in the runtime");
            }
            // configuration an authentication interceptor to set the subject on the work context
            for (InvocationChain chain : wire.getInvocationChains()) {
                Interceptor next = chain.getHeadInterceptor();
                String username = configuration.getUsername();
                String password = configuration.getPassword();
                AuthenticatingInterceptor interceptor = new AuthenticatingInterceptor(username, password, authenticationService, next);
                chain.addInterceptor(0, interceptor);
            }
        }
        holder.add(testName, wire);
    }

    public void detach(JUnitWireSource source, PhysicalWireTarget target) {
    }

}
