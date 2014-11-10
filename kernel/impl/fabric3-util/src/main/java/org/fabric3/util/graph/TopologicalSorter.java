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
 * Performs topological sorts of a directed acyclic graph (DAG).
 */
public interface TopologicalSorter<T> {
    /**
     * Performs a topological sort of the graph.
     *
     * @param dag the DAG to sort
     * @return the total ordered list of vertices.
     * @throws GraphException if a cycle or other error is detected
     */
    List<Vertex<T>> sort(DirectedGraph<T> dag) throws GraphException;

    /**
     * Performs a topological sort of the subgraph reachable from the outgoing edges of the given vertex.
     *
     * @param dag   the DAG to sort
     * @param start the starting vertex.
     * @return the total ordered list of vertice
     * @throws GraphException if a cycle or other error is detected
     */
    List<Vertex<T>> sort(DirectedGraph<T> dag, Vertex<T> start) throws GraphException;

    /**
     * Performs a reverse topological sort of the subgraph reachable from the outgoing edges of the given vertex.
     *
     * @param dag the DAG to sort
     * @return the sorted list of vertices.
     * @throws GraphException if a cycle or other error is detected
     */
    List<Vertex<T>> reverseSort(DirectedGraph<T> dag) throws GraphException;

    /**
     * Performs a topological sort of the subgraph reachable from the outgoing edges of the given vertex.
     *
     * @param dag   the DAG to sort
     * @param start the starting vertex.
     * @return the total ordered list of vertices
     * @throws GraphException if a cycle or other error is detected
     */
    List<Vertex<T>> reverseSort(DirectedGraph<T> dag, Vertex<T> start) throws GraphException;
}
