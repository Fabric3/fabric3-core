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
package org.fabric3.contribution.archive;

import java.net.URI;
import java.net.URL;

import f3.TestProvider;
import junit.framework.TestCase;
import org.fabric3.api.annotation.model.Component;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;

/**
 *
 */
public class ComponentJavaArtifactIntrospectorTestCase extends TestCase {
    private URL url;
    private Contribution contribution;
    private ComponentJavaArtifactIntrospector introspector;
    private DefaultIntrospectionContext context;

    public void testProvider() throws Exception {
        Resource resource = introspector.inspect(TestProvider.class, url, contribution, context);

        assertEquals(Constants.DSL_CONTENT_TYPE, resource.getContentType());
    }

    public void testComponent() throws Exception {
        Resource resource = introspector.inspect(TestComponent.class, url, contribution, context);

        assertEquals(Constants.JAVA_COMPONENT_CONTENT_TYPE, resource.getContentType());
    }

    public void testNoComponent() throws Exception {
        assertNull(introspector.inspect(getClass(), url, contribution, context));

    }

    public void setUp() throws Exception {
        super.setUp();
        introspector = new ComponentJavaArtifactIntrospector();
        url = new URL("file://test");
        contribution = new Contribution(URI.create("test"));
        context = new DefaultIntrospectionContext(URI.create("test"), getClass().getClassLoader());
    }

    @Component(name = "Test", composite = "{foo}bar")
    private static class TestComponent {

    }

}
