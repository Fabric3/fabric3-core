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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.container.component;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.monitor.Monitorable;

/**
 * The runtime instantiation of a component
 */
public interface Component extends Monitorable {

    /**
     * Returns the QName of the deployable composite this component was deployed with.
     *
     * @return the group containing this component
     */
    QName getDeployable();

    /**
     * Returns the component URI.
     *
     * @return the component URI
     */
    URI getUri();

    /**
     * Returns the URI of the contribution the component is contained in.
     *
     * @return the contribution URI
     */
    URI getContributionUri();

    /**
     * Sets the URI of the contribution the component is contained in.
     *
     * @param uri the contribution URI
     */
    void setContributionUri(URI uri);

    /**
     * Starts the component;
     *
     * @throws Fabric3Exception if an error occurs starting the component
     */
    void start() throws Fabric3Exception;

    /**
     * Stops the component.
     *
     * @throws Fabric3Exception if an error occurs stopping the component
     */
    void stop() throws Fabric3Exception;

    /**
     * Used to signal the start of a component configuration update.
     */
    void startUpdate();

    /**
     * Used to signal when a component configuration update is complete.
     */
    void endUpdate();

}
