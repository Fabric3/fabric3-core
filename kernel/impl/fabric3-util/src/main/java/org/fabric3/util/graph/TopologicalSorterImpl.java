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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of a topological sorter.
 */
public class TopologicalSorterImpl<T> implements TopologicalSorter<T> {

    public List<Vertex<T>> sort(DirectedGraph<T> dag) throws CycleException {
        // perform the sort over the entire graph, calculating roots and references for all children
        Map<Vertex<T>, AtomicInteger> vertexMap = new HashMap<>();
        List<Vertex<T>> roots = new ArrayList<>();
        // first pass over the graph to collect vertex references and root vertices
        Set<Vertex<T>> vertices = dag.getVertices();
        for (Vertex<T> v : vertices) {
            int incoming = dag.getIncomingEdges(v).size();
            if (incoming == 0) {
                roots.add(v);
            } else {
                AtomicInteger count = new AtomicInteger();
                count.set(incoming);
                vertexMap.put(v, count);
            }
        }
        // perform the sort
        return sort(dag, vertexMap, roots);
    }


    public List<Vertex<T>> sort(DirectedGraph<T> dag, Vertex<T> start) throws CycleException {
        // perform the sort over the subgraph graph formed from the given vertex, calculating roots and references
        // for its children
        DepthFirstTraverser<T> dfs = new DepthFirstTraverserImpl<>();
        Map<Vertex<T>, AtomicInteger> vertexMap = new HashMap<>();
        List<Vertex<T>> vertices = dfs.traverse(dag, start);
        for (Vertex<T> v : vertices) {
            List<Vertex<T>> outgoing = dag.getOutgoingAdjacentVertices(v);
            for (Vertex<T> child : outgoing) {
                AtomicInteger count = vertexMap.get(child);
                if (count == null) {
                    count = new AtomicInteger();
                    vertexMap.put(child, count);
                }
                count.incrementAndGet();
            }
        }

        List<Vertex<T>> roots = new ArrayList<>();
        roots.add(start);
        // perform the sort
        return sort(dag, vertexMap, roots);
    }

    public List<Vertex<T>> reverseSort(DirectedGraph<T> dag) throws CycleException {
        List<Vertex<T>> sortSequence = sort(dag);
        Collections.reverse(sortSequence);
        return sortSequence;
    }

    public List<Vertex<T>> reverseSort(DirectedGraph<T> dag, Vertex<T> start) throws CycleException {
        List<Vertex<T>> sorted = sort(dag, start);
        Collections.reverse(sorted);
        return sorted;
    }

    /**
     * Performs the sort.
     *
     * @param dag      the DAG to sort
     * @param vertices map of vertices and references
     * @param roots    roots in the graph
     * @return the total ordering calculated by the topological sort
     * @throws CycleException if a cycle is detected
     */
    private List<Vertex<T>> sort(DirectedGraph<T> dag, Map<Vertex<T>, AtomicInteger> vertices, List<Vertex<T>> roots) throws CycleException {
        List<Vertex<T>> visited = new ArrayList<>();
        int num = vertices.size() + roots.size();
        while (!roots.isEmpty()) {
            Vertex<T> v = roots.remove(roots.size() - 1);
            visited.add(v);
            List<Vertex<T>> outgoing = dag.getOutgoingAdjacentVertices(v);
            for (Vertex<T> child : outgoing) {
                AtomicInteger count = vertices.get(child);
                if (count.decrementAndGet() == 0) {
                    // add child to root list as all parents are processed
                    roots.add(child);
                }
            }
        }
        if (visited.size() != num) {
            throw new CycleException();
        }
        return visited;
    }

}
