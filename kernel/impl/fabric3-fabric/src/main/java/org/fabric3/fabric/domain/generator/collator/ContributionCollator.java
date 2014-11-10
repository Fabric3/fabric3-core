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
package org.fabric3.fabric.domain.generator.collator;

import java.util.List;
import java.util.Map;

import org.fabric3.fabric.domain.generator.GenerationType;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Calculates contributions required for a deployment.
 */
public interface ContributionCollator {

    /**
     * Collates contributions for components being deployed or undeployed by zone. That is, the list of components is processed to determine the
     * required set of contributions keyed by zone where the components are to be deployed to or undeployed from.
     *
     * @param components the set of components
     * @param type       the type of generation being performed: incremental deploy, full deploy; or undeploy
     * @return the set of required contributions grouped by zone
     */
    Map<String, List<Contribution>> collateContributions(List<LogicalComponent<?>> components, GenerationType type);
}
