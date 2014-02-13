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
package org.fabric3.implementation.spring.runtime.builder;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.implementation.spring.provision.SpringComponentDefinition;
import org.fabric3.implementation.spring.runtime.component.ContextAnnotationPostProcessor;
import org.fabric3.implementation.spring.runtime.component.SCAApplicationContext;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.spi.container.builder.BuilderException;
import org.fabric3.spi.container.builder.component.ComponentBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spring.spi.ApplicationContextListener;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.w3c.dom.Document;

/**
 * Builds a {@link SpringComponent} from a physical definition. Each SpringComponent contains an application context hierarchy.
 * <p/>
 * The parent context contains object factories for creating wire proxies for references configured on the component. In addition, the parent context also
 * contains system components configured to be aliased as Spring beans.
 * <p/>
 * The child context contains beans defined in the configuration file specified by the location attribute of the Spring component.
 */
@EagerInit
public class SpringComponentBuilder implements ComponentBuilder<SpringComponentDefinition, SpringComponent> {
    private static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;
    private static final QName XSD_BOOLEAN = new QName(XSD_NS, "boolean");
    private static final QName XSD_INT = new QName(XSD_NS, "integer");

    private List<BeanPostProcessor> POST_PROCESSORS = Collections.<BeanPostProcessor>singletonList(new ContextAnnotationPostProcessor());

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
        List<URL> sources = new ArrayList<>();
        if (SpringComponentDefinition.LocationType.JAR == definition.getLocationType()) {
            // jar
            resolveJarSources(definition, classLoader, sources);
        } else if (SpringComponentDefinition.LocationType.DIRECTORY == definition.getLocationType()) {
            // directory
            resolveDirectorySources(definition, classLoader, sources);
        } else {
            // file
            List<String> contextLocations = definition.getContextLocations();
            for (String location : contextLocations) {
                sources.add(classLoader.getResource(location));
            }
        }
        URI componentUri = definition.getComponentUri();
        QName deployable = definition.getDeployable();

        Map<String, Pair> properties = createProperties(definition);

        SCAApplicationContext parent = createParentContext(classLoader, properties);
        Map<String, String> alias = definition.getDefaultReferenceMappings();
        return new SpringComponent(componentUri, deployable, parent, sources, classLoader, validating, alias, POST_PROCESSORS);
    }

    public void dispose(SpringComponentDefinition definition, SpringComponent component) throws BuilderException {
        for (ApplicationContextListener listener : listeners) {
            SCAApplicationContext context = component.getParent();
            listener.onDispose(context);
        }
    }

    private void resolveDirectorySources(SpringComponentDefinition definition, ClassLoader classLoader, List<URL> sources) throws BuilderException {
        List<String> contextLocations = definition.getContextLocations();
        for (String location : contextLocations) {

            URL resource = classLoader.getResource(definition.getBaseLocation());
            if (resource == null) {
                throw new BuilderException("Resource path not found:" + definition.getBaseLocation());
            }
            String path = resource.getPath();
            File filePath = new File(path);
            try {
                URL url = new File(filePath, location).toURI().toURL();
                sources.add(url);
            } catch (MalformedURLException e) {
                throw new BuilderException(e);
            }
        }
    }

    /**
     * Creates a parent application context populated with system components configured to be aliased as Spring beans.
     *
     * @param classLoader the context classloader
     * @param properties  any defined property values keyed by name
     * @return the parent application context
     */
    private SCAApplicationContext createParentContext(ClassLoader classLoader, Map<String, Pair> properties) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            SCAApplicationContext parent = new SCAApplicationContext();
            for (Map.Entry<String, Pair> entry : properties.entrySet()) {
                String name = entry.getKey();
                Pair pair = entry.getValue();
                parent.add(name, pair.getType(), pair.getFactory());
            }
            for (ApplicationContextListener listener : listeners) {
                listener.onCreate(parent);
            }
            return parent;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private void resolveJarSources(SpringComponentDefinition definition, ClassLoader classLoader, List<URL> sources) throws BuilderException {
        try {
            for (String location : definition.getContextLocations()) {
                URL resource = classLoader.getResource(definition.getBaseLocation());
                if (resource == null) {
                    throw new BuilderException("Resource was null: " + definition.getBaseLocation());
                }
                URL url = new URL("jar:" + resource.toExternalForm() + location);
                sources.add(url);

            }
        } catch (MalformedURLException e) {
            throw new BuilderException(e);
        }
    }

    protected Map<String, Pair> createProperties(SpringComponentDefinition definition) throws BuilderException {
        List<PhysicalPropertyDefinition> propertyDefinitions = definition.getPropertyDefinitions();
        Map<String, Pair> values = new HashMap<>();

        for (PhysicalPropertyDefinition propertyDefinition : propertyDefinitions) {
            String name = propertyDefinition.getName();
            if (propertyDefinition.getInstanceValue() != null) {
                SingletonObjectFactory<Object> factory = new SingletonObjectFactory<>(propertyDefinition.getInstanceValue());
                Pair pair = new Pair(Object.class, factory);
                values.put(name, pair);
            } else {
                Document document = propertyDefinition.getValue();
                String value = document.getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
                QName type = propertyDefinition.getType();
                if (XSD_BOOLEAN.equals(type)) {
                    SingletonObjectFactory<Boolean> factory = new SingletonObjectFactory<>(Boolean.valueOf(value));
                    Pair pair = new Pair(Boolean.class, factory);
                    values.put(name, pair);
                } else if (XSD_INT.equals(type)) {
                    SingletonObjectFactory<Integer> factory = new SingletonObjectFactory<>(Integer.valueOf(value));
                    Pair pair = new Pair(Integer.class, factory);
                    values.put(name, pair);
                } else {
                    SingletonObjectFactory<String> factory = new SingletonObjectFactory<>(value);
                    Pair pair = new Pair(String.class, factory);
                    values.put(name, pair);
                }
            }
        }
        return values;
    }

    private class Pair {
        private Class<?> type;
        private ObjectFactory<?> factory;

        private Pair(Class<?> type, ObjectFactory<?> factory) {
            this.type = type;
            this.factory = factory;
        }

        public Class<?> getType() {
            return type;
        }

        public ObjectFactory<?> getFactory() {
            return factory;
        }
    }

}