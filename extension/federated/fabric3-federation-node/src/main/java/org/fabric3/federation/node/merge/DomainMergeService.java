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
package org.fabric3.federation.node.merge;

import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * Performs merge operations against the domain logical model.
 */
public interface DomainMergeService {

    /**
     * Merges the domain snapshot with the live logical domain. Components and channels in the {@link LogicalState#NEW} will be added; those in the {@link
     * LogicalState#MARKED} will be removed.
     *
     * @param snapshot the snapshot to merge
     */
    void merge(LogicalCompositeComponent snapshot);

    /**
     * Drops logical artifacts for the zone.
     *
     * @param zone the zone to drop
     */
    void drop(String zone);
}
