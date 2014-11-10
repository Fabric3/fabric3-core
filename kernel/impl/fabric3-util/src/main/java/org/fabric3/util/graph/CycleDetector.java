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
package org.fabric3.util.graph;

import java.util.List;

/**
 * Detects cycles in a directed graph.
 */
public interface CycleDetector<T> {

    /**
     * Determines if a directed graph associated with this detector has cycles.
     *
     * @param graph the graph to check
     * @return true if a directed graph has cycles
     */
    boolean hasCycles(DirectedGraph<T> graph);

    /**
     * Returns the subgraph containing cycles in the graph associated with this detector.
     *
     * @param graph the graph to check
     * @return the subgraph
     */
    DirectedGraph<T> findCycleSubgraph(DirectedGraph<T> graph);

    /**
     * Finds and returns cycles in the graph.
     *
     * @param graph the graph
     * @return found cycles or an empty collection
     */
    List<Cycle<T>> findCycles(DirectedGraph<T> graph);

}
