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
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.spring.provision.PhysicalSpringComponent;
import org.fabric3.implementation.spring.runtime.component.ContextAnnotationPostProcessor;
import org.fabric3.implementation.spring.runtime.component.SCAApplicationContext;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.container.builder.ComponentBuilder;
import org.fabric3.spi.model.physical.PhysicalProperty;
import org.fabric3.spring.spi.ApplicationContextListener;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.w3c.dom.Document;

/**
 * Builds a {@link org.fabric3.implementation.spring.runtime.component.SpringComponent} from a physical definition. Each SpringComponent contains an application
 * context hierarchy.  The parent context contains object factories for creating wire proxies for references configured on the component. In addition, the
 * parent context also contains system components configured to be aliased as Spring beans.  The child context contains beans defined in the configuration file
 * specified by the location attribute of the Spring component.
 */
@EagerInit
public class SpringComponentBuilder implements ComponentBuilder<PhysicalSpringComponent, org.fabric3.implementation.spring.runtime.component.SpringComponent> {
    private static final String XSD_NS = XMLConstants.W3C_XML_SCHEMA_NS_URI;
    private static final QName XSD_BOOLEAN = new QName(XSD_NS, "boolean");
    private static final QName XSD_INT = new QName(XSD_NS, "integer");

    private List<BeanPostProcessor> POST_PROCESSORS = Collections.<BeanPostProcessor>singletonList(new ContextAnnotationPostProcessor());

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

    public org.fabric3.implementation.spring.runtime.component.SpringComponent build(PhysicalSpringComponent physicalComponent) throws Fabric3Exception {
        ClassLoader classLoader = physicalComponent.getClassLoader();
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
        if (PhysicalSpringComponent.LocationType.JAR == physicalComponent.getLocationType()) {
            // jar
            resolveJarSources(physicalComponent, classLoader, sources);
        } else if (PhysicalSpringComponent.LocationType.DIRECTORY == physicalComponent.getLocationType()) {
            // directory
            resolveDirectorySources(physicalComponent, classLoader, sources);
        } else {
            // file
            List<String> contextLocations = physicalComponent.getContextLocations();
            for (String location : contextLocations) {
                sources.add(classLoader.getResource(location));
            }
        }
        URI componentUri = physicalComponent.getComponentUri();

        Map<String, Pair> properties = createProperties(physicalComponent);

        SCAApplicationContext parent = createParentContext(classLoader, properties);
        Map<String, String> alias = physicalComponent.getDefaultReferenceMappings();
        URI contributionUri = physicalComponent.getContributionUri();
        return new org.fabric3.implementation.spring.runtime.component.SpringComponent(componentUri,
                                                                                       parent,
                                                                                       sources,
                                                                                       classLoader,
                                                                                       validating,
                                                                                       alias,
                                                                                       POST_PROCESSORS, contributionUri);
    }

    public void dispose(PhysicalSpringComponent physicalComponent, org.fabric3.implementation.spring.runtime.component.SpringComponent component) {
        for (ApplicationContextListener listener : listeners) {
            SCAApplicationContext context = component.getParent();
            listener.onDispose(context);
        }
    }

    private void resolveDirectorySources(PhysicalSpringComponent springComponent, ClassLoader classLoader, List<URL> sources) throws Fabric3Exception {
        List<String> contextLocations = springComponent.getContextLocations();
        for (String location : contextLocations) {

            URL resource = classLoader.getResource(springComponent.getBaseLocation());
            if (resource == null) {
                throw new Fabric3Exception("Resource path not found:" + springComponent.getBaseLocation());
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
                parent.add(name, pair.getType(), pair.getSupplier());
            }
            for (ApplicationContextListener listener : listeners) {
                listener.onCreate(parent);
            }
            return parent;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private void resolveJarSources(PhysicalSpringComponent springComponent, ClassLoader classLoader, List<URL> sources) throws Fabric3Exception {
        try {
            for (String location : springComponent.getContextLocations()) {
                URL resource = classLoader.getResource(springComponent.getBaseLocation());
                if (resource == null) {
                    throw new Fabric3Exception("Resource was null: " + springComponent.getBaseLocation());
                }
                URL url = new URL("jar:" + resource.toExternalForm() + location);
                sources.add(url);

            }
        } catch (MalformedURLException e) {
            throw new Fabric3Exception(e);
        }
    }

    protected Map<String, Pair> createProperties(PhysicalSpringComponent springComponent) throws Fabric3Exception {
        List<PhysicalProperty> properties = springComponent.getProperties();
        Map<String, Pair> values = new HashMap<>();

        for (PhysicalProperty property : properties) {
            String name = property.getName();
            if (property.getInstanceValue() != null) {
                Pair pair = new Pair(Object.class, property::getInstanceValue);
                values.put(name, pair);
            } else {
                Document document = property.getValue();
                String value = document.getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
                QName type = property.getType();
                if (XSD_BOOLEAN.equals(type)) {
                    Pair pair = new Pair(Boolean.class, () -> Boolean.valueOf(value));
                    values.put(name, pair);
                } else if (XSD_INT.equals(type)) {
                    Pair pair = new Pair(Integer.class, () -> Boolean.valueOf(value));
                    values.put(name, pair);
                } else {
                    Pair pair = new Pair(String.class, () -> value);
                    values.put(name, pair);
                }
            }
        }
        return values;
    }

    private class Pair {
        private Class<?> type;
        private Supplier<?> supplier;

        private Pair(Class<?> type, Supplier<?> supplier) {
            this.type = type;
            this.supplier = supplier;
        }

        public Class<?> getType() {
            return type;
        }

        public Supplier<?> getSupplier() {
            return supplier;
        }
    }

}