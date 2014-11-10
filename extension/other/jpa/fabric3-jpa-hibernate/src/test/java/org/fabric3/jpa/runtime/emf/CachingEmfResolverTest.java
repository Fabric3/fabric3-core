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
package org.fabric3.jpa.runtime.emf;

import java.util.Collections;
import javax.persistence.EntityManagerFactory;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.jpa.api.PersistenceOverrides;

/**
 *
 */
public class CachingEmfResolverTest extends TestCase {

    private EntityManagerFactoryResolver emfResolver;

    protected void setUp() throws Exception {
        PersistenceContextParserImpl parser = null;
        CacheMonitor monitor = EasyMock.createNiceMock(CacheMonitor.class);
        DefaultEntityManagerFactoryCache cache = new DefaultEntityManagerFactoryCache(monitor);
        emfResolver = new CachingEntityManagerFactoryResolver(parser, cache);
    }

    public void testBuild() throws Exception {

        ClassLoader classLoader = getClass().getClassLoader();
        PersistenceOverrides overrides = new PersistenceOverrides("test", Collections.<String, String>emptyMap());
        EntityManagerFactory emf = emfResolver.resolve("test", overrides, classLoader);
        assertNotNull(emf);
    }

}
