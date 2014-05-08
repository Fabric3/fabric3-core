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
        String name = TestProvider.class.getName().replace(".", "/") + ".class";
        Resource resource = introspector.inspect(name, url, contribution, context);

        assertEquals(Constants.DSL_CONTENT_TYPE, resource.getContentType());
    }

    public void testComponent() throws Exception {
        String name = TestComponent.class.getName().replace(".", "/") + ".class";
        Resource resource = introspector.inspect(name, url, contribution, context);

        assertEquals(Constants.JAVA_COMPONENT_CONTENT_TYPE, resource.getContentType());
    }

    public void testNoComponent() throws Exception {
        String name = getClass().getName().replace(".", "/") + ".class";
        assertNull(introspector.inspect(name, url, contribution, context));

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
