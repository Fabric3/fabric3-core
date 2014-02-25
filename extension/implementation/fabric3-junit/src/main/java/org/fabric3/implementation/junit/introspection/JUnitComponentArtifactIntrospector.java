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
package org.fabric3.implementation.junit.introspection;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URL;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaArtifactIntrospector;
import org.fabric3.spi.contribution.JavaSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.junit.runner.RunWith;

/**
 * Introspects a class to determine if it is a JUnit component.
 */
public class JUnitComponentArtifactIntrospector implements JavaArtifactIntrospector {
    private static final QName TEST_COMPOSITE = new QName(Namespaces.F3, "TestComposite");

    public Resource inspect(String name, URL url, Contribution contribution, ClassLoader loader) {
        try {
            int extensionIndex = name.lastIndexOf('.');
            if (extensionIndex < 1) {
                throw new AssertionError("Not a class: " + name);
            }
            if (contribution.getManifest().isExtension()) {
                return null;
            }
            String className = name.substring(0, extensionIndex).replace(File.separator, ".");
            Class<?> clazz = loader.loadClass(className);
            if (clazz.isAnnotationPresent(Component.class) || !clazz.isAnnotationPresent(RunWith.class)) {
                // not a Junit component or labeled as a component, avoid creating a duplicate
                return null;
            }
            UrlSource source = new UrlSource(url);
            Resource resource = new Resource(contribution, source, Constants.JAVA_COMPONENT_CONTENT_TYPE);
            JavaSymbol symbol = new JavaSymbol(className);
            ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, clazz);
            resourceElement.setMetadata(TEST_COMPOSITE);
            resource.addResourceElement(resourceElement);
            contribution.addResource(resource);
            return resource;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // ignore since the class may reference another class not present in the contribution
        }
        return null;
    }
}
