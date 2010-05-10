/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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

import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

import org.springframework.beans.BeansException;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.Component;

/**
 * The runtime representation of a Spring component. A Spring component has and associated application context.
 *
 * @version $Rev$ $Date$
 */
public class SpringComponent implements Component {
    private URI uri;
    private QName deployable;
    private URL source;
    private ClassLoader classLoader;
    private URI classLoaderId;
    private int state = UNINITIALIZED;
    private GenericXmlApplicationContext applicationContext;
    private SCAApplicationContext parent;

    /**
     * Constructor.
     *
     * @param uri         the component URI.
     * @param deployable  the composite the component is deployed with
     * @param parent      the parent application context for resolving wire and event stream proxies
     * @param source      the location of the application context XML configuration.
     * @param classLoader the contribution classloader containing user-defined application classes and resources
     */
    public SpringComponent(URI uri, QName deployable, SCAApplicationContext parent, URL source, ClassLoader classLoader) {
        this.uri = uri;
        this.deployable = deployable;
        this.parent = parent;
        this.source = source;
        this.classLoader = classLoader;
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

    public int getLifecycleState() {
        return state;
    }

    public void start() throws SpringComponentStartException {
        applicationContext = new GenericXmlApplicationContext();
        try {
            // initialize the parent context
            parent.refresh();
            parent.start();

            // initialize the context associated with the component
            applicationContext.setParent(parent);
            applicationContext.setClassLoader(classLoader);
            Resource resource = new UrlResource(source);
            applicationContext.load(resource);
            applicationContext.refresh();
            applicationContext.start();
        } catch (BeansException e) {
            throw new SpringComponentStartException("Error starting component: " + getUri(), e);
        }
        state = INITIALIZED;
    }

    public void stop() {
        state = STOPPED;
        applicationContext.stop();
        parent.stop();
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
    public void dettach(String name) {
        parent.remove(name);
    }

    public Object getBean(String name) throws SpringBeanNotFoundException {
        return applicationContext.getBean(name);
    }

}
