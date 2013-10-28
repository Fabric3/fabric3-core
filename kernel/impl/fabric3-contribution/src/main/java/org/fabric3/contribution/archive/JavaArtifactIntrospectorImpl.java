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

import java.net.URL;

import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaArtifactIntrospector;
import org.fabric3.spi.contribution.JavaSymbol;
import org.fabric3.spi.contribution.ProviderSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.Symbol;

/**
 *
 */
public class JavaArtifactIntrospectorImpl implements JavaArtifactIntrospector {

    public Resource inspect(String name, URL url, Contribution contribution, ClassLoader loader) {
        try {
            int extensionIndex = name.lastIndexOf('.');
            if (extensionIndex < 1) {
                throw new AssertionError("Not a class: " + name);
            }
            String className = name.substring(0, extensionIndex).replace("/", ".");
            if (isProvider(className)) {
                // the class is a model provider
                UrlSource source = new UrlSource(url);
                Resource resource = new Resource(contribution, source, Constants.DSL_CONTENT_TYPE);
                ProviderSymbol symbol = new ProviderSymbol(className);
                ResourceElement<Symbol, Object> element = new ResourceElement<Symbol, Object>(symbol);
                resource.addResourceElement(element);
                contribution.addResource(resource);
                return resource;
            } else if (!contribution.getManifest().isExtension()) {
                Class<?> clazz = loader.loadClass(className);
                if (clazz.isAnnotationPresent(Component.class)) {
                    // class is a component
                    UrlSource source = new UrlSource(url);
                    Resource resource = new Resource(contribution, source, Constants.JAVA_COMPONENT_CONTENT_TYPE);
                    JavaSymbol symbol = new JavaSymbol(className);
                    ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, clazz);
                    resource.addResourceElement(resourceElement);
                    contribution.addResource(resource);
                    return resource;
                }
            }
            return null;
        } catch (ClassNotFoundException e) {
            // ignore since the class may reference another class not present in the contribution
        } catch (NoClassDefFoundError e) {
            // ignore since the class may reference another class not present in the contribution
        }
        return null;
    }

    private boolean isProvider(String name) {
        return name.startsWith("f3.") && name.endsWith("Provider");
    }

}
