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
import java.util.List;
import java.util.Set;

/**
 * Detects cycles in a directed graph.
 */
public class CycleDetectorImpl<T> implements CycleDetector<T> {
    private DepthFirstTraverser<T> traverser;

    public CycleDetectorImpl() {
        traverser = new DepthFirstTraverserImpl<>();
    }

    public boolean hasCycles(DirectedGraph<T> graph) {
        for (Vertex<T> vertex : graph.getVertices()) {
            if (isCycle(graph, vertex)) {
                return true;
            }
        }
        return false;
    }

    public DirectedGraph<T> findCycleSubgraph(DirectedGraph<T> graph) {
        DirectedGraph<T> subGraph = new DirectedGraphImpl<>();
        for (Edge<T> edge : graph.getEdges()) {
            if (isPath(graph, edge.getSink(), edge.getSource())) {
                subGraph.add(edge);
            }
        }
        return subGraph;
    }

    public List<Cycle<T>> findCycles(DirectedGraph<T> graph) {
        List<Cycle<T>> cycles = new ArrayList<>();
        for (Edge<T> edge : graph.getEdges()) {
            List<Vertex<T>> path = getPath(graph, edge.getSink(), edge.getSource());
            if (!path.isEmpty()) {
                Cycle<T> cycle = searchCycle(cycles, edge);
                if (cycle == null) {
                    cycle = new Cycle<>();
                    cycle.setOriginPath(path);
                    cycles.add(cycle);
                } else {
                    cycle.setBackPath(path);
                }
            }
        }
        return cycles;
    }

    private Cycle<T> searchCycle(List<Cycle<T>> cycles, Edge<T> edge) {
        for (Cycle<T> cycle : cycles) {
            List<Vertex<T>> path = cycle.getOriginPath();
            Vertex<T> vertex = path.get(0);
            if (vertex.equals(edge.getSink())) {
                return cycle;
            }
        }
        return null;
    }

    private boolean isCycle(DirectedGraph<T> graph, Vertex<T> from) {
        Set<Edge<T>> edges = graph.getOutgoingEdges(from);
        for (Edge<T> edge : edges) {
            Vertex<T> opposite = edge.getOppositeVertex(from);
            if (isPath(graph, opposite, from)) {
                // cycle found
                return true;
            }
        }
        return false;
    }


    /**
     * Returns true if a path exists between two vertices.
     *
     * @param graph the graph to search
     * @param start the starting vertex
     * @param end   the ending vertex
     * @return true if a path exists between two vertices
     */
    private boolean isPath(DirectedGraph<T> graph, Vertex<T> start, Vertex<T> end) {
        return !getPath(graph, start, end).isEmpty();
    }

    /**
     * Returns the ordered list of vertices traversed for a path defined by the given start and end vertices. If no path exists, an empty colleciton
     * is returned.
     *
     * @param graph the graph to search
     * @param start the starting vertex
     * @param end   the ending vertex
     * @return the ordered collection of vertices or an empty collection if no path exists
     */
    private List<Vertex<T>> getPath(DirectedGraph<T> graph, Vertex<T> start, Vertex<T> end) {
        List<Vertex<T>> path = traverser.traversePath(graph, start, end);
        // reverse the order to it goes from source to end destination
        Collections.reverse(path);
        return path;
    }

}
