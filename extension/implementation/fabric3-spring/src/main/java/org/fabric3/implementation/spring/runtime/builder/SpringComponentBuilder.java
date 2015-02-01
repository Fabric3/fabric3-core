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

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.spring.provision.SpringComponentDefinition;
import org.fabric3.implementation.spring.runtime.component.ContextAnnotationPostProcessor;
import org.fabric3.implementation.spring.runtime.component.SCAApplicationContext;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.builder.component.ComponentBuilder;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.fabric3.spi.container.objectfactory.SingletonObjectFactory;
import org.fabric3.spi.model.physical.PhysicalPropertyDefinition;
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

    public SpringComponent build(SpringComponentDefinition definition) throws Fabric3Exception {
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

    public void dispose(SpringComponentDefinition definition, SpringComponent component) throws Fabric3Exception {
        for (ApplicationContextListener listener : listeners) {
            SCAApplicationContext context = component.getParent();
            listener.onDispose(context);
        }
    }

    private void resolveDirectorySources(SpringComponentDefinition definition, ClassLoader classLoader, List<URL> sources) throws Fabric3Exception {
        List<String> contextLocations = definition.getContextLocations();
        for (String location : contextLocations) {

            URL resource = classLoader.getResource(definition.getBaseLocation());
            if (resource == null) {
                throw new Fabric3Exception("Resource path not found:" + definition.getBaseLocation());
            }
            String path = resource.getPath();
            File filePath = new File(path);
            try {
                URL url = new File(filePath, location).toURI().toURL();
                sources.add(url);
            } catch (MalformedURLException e) {
                throw new Fabric3Exception(e);
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

    private void resolveJarSources(SpringComponentDefinition definition, ClassLoader classLoader, List<URL> sources) throws Fabric3Exception {
        try {
            for (String location : definition.getContextLocations()) {
                URL resource = classLoader.getResource(definition.getBaseLocation());
                if (resource == null) {
                    throw new Fabric3Exception("Resource was null: " + definition.getBaseLocation());
                }
                URL url = new URL("jar:" + resource.toExternalForm() + location);
                sources.add(url);

            }
        } catch (MalformedURLException e) {
            throw new Fabric3Exception(e);
        }
    }

    protected Map<String, Pair> createProperties(SpringComponentDefinition definition) throws Fabric3Exception {
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