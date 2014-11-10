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
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;

import junit.framework.TestCase;

/**
 *
 */
public class ProviderRegistryImplTestCase extends TestCase {
    private ProviderRegistryImpl registry = new ProviderRegistryImpl();

    public void testRegisterNameFilter() throws Exception {
        URI uri = URI.create("filter");
        Object filter = new Object();
        registry.registerNameFilter(uri, TestNameBinding.class, filter);

        assertEquals(filter, registry.getNameFilters(TestNameBinding.class).iterator().next());

        assertEquals(filter, registry.unregisterNameFilter(uri, TestNameBinding.class));
        assertNull(registry.unregisterNameFilter(uri, TestNameBinding.class));

    }

    public void testRegisterGlobalFilter() throws Exception {
        URI uri = URI.create("filter");
        Object filter = new Object();
        registry.registerGlobalProvider(uri, filter);

        assertEquals(filter, registry.getGlobalProvider().iterator().next());

        assertEquals(filter, registry.unregisterGlobalFilter(uri));
        assertNull(registry.unregisterGlobalFilter(uri));

    }

    @NameBinding
    @Target({ElementType.TYPE, ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestNameBinding {
    }

}
