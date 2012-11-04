/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.spring.runtime.builder;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.implementation.spring.provision.SpringComponentDefinition;
import org.fabric3.implementation.spring.runtime.component.SCAApplicationContext;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spring.spi.ApplicationContextListener;

/**
 * Builds a {@link SpringComponent} from a physical definition. Each SpringComponent contains an application context hierarchy.
 * <p/>
 * The parent context contains object factories for creating wire proxies for references configured on the component. In addition, the parent context
 * also contains system components configured to be aliased as Spring beans.
 * <p/>
 * The child context contains beans defined in the configuration file specified by the location attribute of the Spring component.
 */
@EagerInit
public class SpringComponentBuilder implements ComponentBuilder<SpringComponentDefinition, SpringComponent> {

    private ClassLoaderRegistry classLoaderRegistry;
    private boolean validating = true;
    private List<ApplicationContextListener> listeners = Collections.emptyList();

    @Property(required = false)
    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    @Reference(required = false)
    public void setListeners(List<ApplicationContextListener> listeners) {
        this.listeners = listeners;
    }

    public SpringComponentBuilder(@Reference ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public SpringComponent build(SpringComponentDefinition definition) throws BuilderException {
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());
        if (classLoader instanceof MultiParentClassLoader) {
            // add the extension classloader as a parent of the app classloader since Spring classes must be visible to the application
            // TODO add a filtering classloader to only expose specific Spring packages
            MultiParentClassLoader cl = (MultiParentClassLoader) classLoader;
            ClassLoader springClassLoader = getClass().getClassLoader();
            if (!cl.getParents().contains(springClassLoader)) {
                cl.addParent(springClassLoader);
            }
        }
        URL source = classLoader.getResource(definition.getLocation());
        URI componentUri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        SCAApplicationContext parent = createParentContext(classLoader);
        return new SpringComponent(componentUri, deployable, parent, source, classLoader, validating);
    }

    public void dispose(SpringComponentDefinition definition, SpringComponent component) throws BuilderException {
        for (ApplicationContextListener listener : listeners) {
            SCAApplicationContext context = component.getParent();
            listener.onDispose(context);
        }
    }

    /**
     * Creates a parent application context populated with system components configured to be aliased as Spring beans.
     *
     * @param classLoader the context classloader
     * @return the parent application context
     */
    private SCAApplicationContext createParentContext(ClassLoader classLoader) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            SCAApplicationContext parent = new SCAApplicationContext();
            for (ApplicationContextListener listener : listeners) {
                listener.onCreate(parent);
            }
            return parent;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}