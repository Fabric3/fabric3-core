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
package org.fabric3.implementation.spring.runtime.component;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.fabric3.api.annotation.monitor.MonitorLevel;
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

    public void start() throws SpringComponentStartException {
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
                throw new SpringComponentStartException("Error starting component: " + getUri(), e);
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
