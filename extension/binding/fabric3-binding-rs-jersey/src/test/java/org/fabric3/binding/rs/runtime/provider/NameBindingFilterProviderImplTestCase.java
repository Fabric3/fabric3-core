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
package org.fabric3.binding.rs.runtime.provider;

import javax.ws.rs.NameBinding;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class NameBindingFilterProviderImplTestCase extends TestCase {

    private Method method;

    private ProviderRegistry providerRegistry;
    private NameBindingFilterProviderImpl provider;
    private FeatureContext featureContext;

    public void testApplyFilter() throws Exception {

        ResourceInfo info = new ResourceInfo() {
            public Method getResourceMethod() {
                return method;
            }

            public Class<?> getResourceClass() {
                return TestResource.class;
            }
        };

        ContainerRequestFilter filter = EasyMock.createMock(ContainerRequestFilter.class);
        Collection<Object> filters = Collections.<Object>singletonList(filter);
        EasyMock.expect(providerRegistry.getNameFilters(EasyMock.eq(TestNameBinding.class))).andReturn(filters);

        EasyMock.expect(featureContext.register(EasyMock.eq(filter))).andReturn(null);

        EasyMock.replay(providerRegistry, featureContext);

        provider.configure(info, featureContext);

        EasyMock.verify(providerRegistry, featureContext);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        providerRegistry = EasyMock.createMock(ProviderRegistry.class);
        provider = new NameBindingFilterProviderImpl(providerRegistry);

        featureContext = EasyMock.createMock(FeatureContext.class);

        method = TestResource.class.getMethod("getMessage");
    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestNameBinding {
    }

    @TestNameBinding
    public class TestResource {

        public String getMessage() {
            return "test";
        }
    }

}
