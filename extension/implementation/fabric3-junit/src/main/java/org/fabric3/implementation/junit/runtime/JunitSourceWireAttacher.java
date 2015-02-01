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
import org.fabric3.implementation.junit.provision.JUnitWireSourceDefinition;
import org.fabric3.implementation.pojo.builder.PojoSourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.container.builder.component.SourceWireAttacher;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.InvocationChain;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.security.AuthenticationService;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.test.spi.TestWireHolder;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class JunitSourceWireAttacher extends PojoSourceWireAttacher implements SourceWireAttacher<JUnitWireSourceDefinition> {
    private TestWireHolder holder;
    private AuthenticationService authenticationService;

    public JunitSourceWireAttacher(@Reference ClassLoaderRegistry classLoaderRegistry,
                                   @Reference TransformerRegistry transformerRegistry,
                                   @Reference TestWireHolder holder) {
        super(transformerRegistry, classLoaderRegistry);
        this.holder = holder;
    }

    @Reference(required = false)
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void attach(JUnitWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws Fabric3Exception {
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

    public void attachObjectFactory(JUnitWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

    public void detach(JUnitWireSourceDefinition source, PhysicalWireTargetDefinition target) throws Fabric3Exception {
    }

    public void detachObjectFactory(JUnitWireSourceDefinition source, PhysicalWireTargetDefinition target) throws Fabric3Exception {
    }


}
