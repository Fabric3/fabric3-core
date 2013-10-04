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
package org.fabric3.implementation.mock.runtime;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.spi.container.component.AtomicComponent;
import org.fabric3.spi.container.component.InstanceLifecycleException;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 *
 */
public class MockComponent implements AtomicComponent {

    private final URI componentId;
    private final ObjectFactory<Object> objectFactory;
    private URI classLoaderId;

    public MockComponent(URI componentId, ObjectFactory<Object> objectFactory) {
        this.componentId = componentId;
        this.objectFactory = objectFactory;
    }

    public URI getUri() {
        return componentId;
    }

    public URI getClassLoaderId() {
        return classLoaderId;
    }

    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    public ObjectFactory<Object> createObjectFactory() {
        return objectFactory;
    }

    public Object getInstance() throws InstanceLifecycleException {
        try {
            return objectFactory.getInstance();
        } catch (ObjectCreationException e) {
            throw new InstanceLifecycleException("Error creating instance for: " + componentId, e);
        }
    }

    public void releaseInstance(Object instance) {

    }

    public void start() {

    }

    public void stop() {

    }

    public void startUpdate() {

    }

    public void endUpdate() {

    }

    public QName getDeployable() {
        return null;
    }

    public String getName() {
        return componentId.toString();
    }

    public MonitorLevel getLevel() {
        return MonitorLevel.INFO;
    }

    public void setLevel(MonitorLevel level) {

    }
}
