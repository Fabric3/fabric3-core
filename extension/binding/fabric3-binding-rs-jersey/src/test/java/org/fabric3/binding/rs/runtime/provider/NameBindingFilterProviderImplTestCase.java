/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
