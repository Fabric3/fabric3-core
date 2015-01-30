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
package org.fabric3.implementation.spring.runtime.component;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.container.ContainerException;
import org.fabric3.spi.container.component.Component;
import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * The runtime representation of a Spring component. A Spring component has an associated application context that manages Spring beans.
 */
public class SpringComponent implements Component {
    private URI uri;
    private QName deployable;
    private List<URL> sources;
    private ClassLoader classLoader;
    private URI classLoaderId;
    private GenericXmlApplicationContext applicationContext;
    private SCAApplicationContext parent;
    private MonitorLevel level = MonitorLevel.INFO;
    private boolean validating;
    private Map<String, String> alias;
    private List<BeanPostProcessor> processors;

    /**
     * Constructor.
     *
     * @param uri         the component URI.
     * @param deployable  the composite the component is deployed with
     * @param parent      the parent application context for resolving wire and event stream proxies
     * @param sources     the location of the application contexts
     * @param classLoader the contribution classloader containing user-defined application classes and resources
     * @param validating  true if application context validation should be done
     * @param alias       bean aliases derived from the default attribute of an SCA reference tag
     * @param processors  bean post processors
     */
    public SpringComponent(URI uri,
                           QName deployable,
                           SCAApplicationContext parent,
                           List<URL> sources,
                           ClassLoader classLoader,
                           boolean validating,
                           Map<String, String> alias,
                           List<BeanPostProcessor> processors) {
        this.uri = uri;
        this.deployable = deployable;
        this.parent = parent;
        this.sources = sources;
        this.classLoader = classLoader;
        this.validating = validating;
        this.alias = alias;
        this.processors = processors;
    }

    public URI getUri() {
        return uri;
    }

    public QName getDeployable() {
        return deployable;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI id) {
        this.classLoaderId = id;
    }

    public String getName() {
        return uri.toString();
    }

    public MonitorLevel getLevel() {
        return level;
    }

    public void setLevel(MonitorLevel level) {
        this.level = level;
    }

    public void start() throws ContainerException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            applicationContext = new GenericXmlApplicationContext();
            applicationContext.setValidating(validating);

            for (Map.Entry<String, String> entry : alias.entrySet()) {
                // register bean aliases derived from any default reference values
                applicationContext.registerAlias(entry.getKey(), entry.getValue());
            }

            try {
                // initialize the parent context
                parent.refresh();
                parent.start();

                // initialize the context associated with the component
                applicationContext.setParent(parent);
                applicationContext.setClassLoader(classLoader);

                // load application contexts
                Resource[] resources = new Resource[sources.size()];
                for (int i = 0; i < sources.size(); i++) {
                    URL url = sources.get(i);
                    resources[i] = new UrlResource(url);
                }
                for (BeanPostProcessor processor : processors) {
                    applicationContext.getBeanFactory().addBeanPostProcessor(processor);
                }
                applicationContext.load(resources);
                applicationContext.refresh();
                applicationContext.start();
            } catch (BeansException e) {
                throw new ContainerException("Error starting component: " + getUri(), e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void stop() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            applicationContext.stop();
            parent.stop();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    public void startUpdate() {

    }

    public void endUpdate() {

    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Adds an object factory for a wire or producer proxy.
     *
     * @param name    the reference or producer name
     * @param type    the interface type implemented by the proxy
     * @param factory the object factory
     */
    public void attach(String name, Class<?> type, ObjectFactory factory) {
        parent.add(name, type, factory);
    }

    /**
     * Removes an object factory for a wire or producer proxy
     *
     * @param name the reference or producer name
     */
    public void detach(String name) {
        parent.remove(name);
    }

    /**
     * Returns the parent application context.
     *
     * @return the parent application context
     */
    public SCAApplicationContext getParent() {
        return parent;
    }

    /**
     * Returns a bean instance for the given bean name.
     *
     * @param name the bean name
     * @return a bean instance
     */
    public Object getBean(String name) {
        if (applicationContext == null) {
            throw new IllegalStateException("Attempt to access a bean the Spring component has been started: " + name);
        }
        return applicationContext.getBean(name);
    }

    /**
     * Returns the bean implementation class for the given bean name.
     *
     * @param name the bean name
     * @return a bean implementation class
     */
    public Class<?> getBeanClass(String name) {
        if (applicationContext == null) {
            throw new IllegalStateException("Attempt to access a bean before the Spring component has been started: " + name);
        }
        String beanClassName = applicationContext.getBeanDefinition(name).getBeanClassName();
        try {
            return classLoader.loadClass(beanClassName);
        } catch (ClassNotFoundException e) {
            // this should not happen at this point
            throw new AssertionError(e);
        }
    }

}
