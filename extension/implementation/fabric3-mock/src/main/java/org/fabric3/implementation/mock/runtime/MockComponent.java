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
package org.fabric3.implementation.mock.runtime;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.annotation.monitor.MonitorLevel;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.container.component.AtomicComponent;
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

    public Object getInstance() throws Fabric3Exception {
        return objectFactory.getInstance();
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
