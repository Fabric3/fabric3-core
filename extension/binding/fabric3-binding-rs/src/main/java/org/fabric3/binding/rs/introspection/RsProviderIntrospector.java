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
package org.fabric3.binding.rs.introspection;

import javax.ws.rs.ext.Provider;
import java.net.URL;

import org.fabric3.api.annotation.model.Component;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.JavaArtifactIntrospector;
import org.fabric3.spi.contribution.JavaSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Creates a component resource for classes annotated with {link @Provider}. If a class is also annotated with {@link Component} it will be skipped as the
 * default introspector will be triggered.
 */
@EagerInit
public class RsProviderIntrospector implements JavaArtifactIntrospector {

    public Resource inspect(String name, URL url, Contribution contribution, IntrospectionContext context) {
        try {
            int extensionIndex = name.lastIndexOf('.');
            if (extensionIndex < 1) {
                throw new AssertionError("Not a class: " + name);
            }
            String className = name.substring(0, extensionIndex).replace("/", ".");
            Class<?> clazz = context.getClassLoader().loadClass(className);
            if (!clazz.isAnnotationPresent(Provider.class) || clazz.isAnnotationPresent(Component.class)) {
                // not a provider or already configured as a component
                return null;
            }

            UrlSource source = new UrlSource(url);
            Resource resource = new Resource(contribution, source, Constants.JAVA_COMPONENT_CONTENT_TYPE);
            JavaSymbol symbol = new JavaSymbol(className);
            ResourceElement<JavaSymbol, Class<?>> resourceElement = new ResourceElement<JavaSymbol, Class<?>>(symbol, clazz);
            resource.addResourceElement(resourceElement);
            return resource;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // ignore since the class may reference another class not present in the contribution
        }
        return null;
    }

}
