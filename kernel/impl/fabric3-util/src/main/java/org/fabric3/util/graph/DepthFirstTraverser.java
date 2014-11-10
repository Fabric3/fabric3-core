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
 * Conducts a depth first graph traversal, returning an ordered list of visited vertices.
 */
public interface DepthFirstTraverser<T> {

    /**
     * Traverse the graph starting at the given vertex.
     *
     * @param graph the graph
     * @param start the starting vertex
     * @return the vertices sorted depth-first
     */
    List<Vertex<T>> traverse(DirectedGraph<T> graph, Vertex<T> start);

    /**
     * Traverse the graph starting at a vertex and ending at another.
     *
     * @param graph the graph
     * @param start the starting vertex
     * @param end   the ending vertex
     * @return the vertices sorted depth-first or an empty list if no path between the vertices exists
     */
    List<Vertex<T>> traversePath(DirectedGraph<T> graph, Vertex<T> start, Vertex<T> end);

}
