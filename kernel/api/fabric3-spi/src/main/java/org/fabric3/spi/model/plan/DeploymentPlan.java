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
package org.fabric3.spi.model.plan;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Fabric3 deployment plan. Deployment plans are used to map a logical assembly to a physical topology during deployment. For example, a
 * deployment plan may map deployable composites to domain zones.
 */
public class DeploymentPlan implements Serializable {
    private static final long serialVersionUID = 4925927937202340746L;

    private String name;
    private Map<QName, String> deployableMappings = new HashMap<>();

    /**
     * Constructor.
     *
     * @param name the unique deployment plan name
     */
    public DeploymentPlan(String name) {
        this.name = name;
    }

    /**
     * Gets the deployment plan name.
     *
     * @return the deployment plan name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the deployable composite name to zone mappings.
     *
     * @return the deployable composite name to zone mappings
     */
    public Map<QName, String> getDeployableMappings() {
        return deployableMappings;
    }

    /**
     * Sets a deployable composite name to zone mapping
     *
     * @param name the deployable composite name
     * @param zone the zone name
     */
    public void addDeployableMapping(QName name, String zone) {
        deployableMappings.put(name, zone);
    }
}
