/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.util.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Detects cycles in a directed graph.
 *
 * @version $Rev$ $Date$
 */
public class CycleDetectorImpl<T> implements CycleDetector<T> {
    private DepthFirstTraverser<T> traverser;

    public CycleDetectorImpl() {
        traverser = new DepthFirstTraverserImpl<T>();
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
        DirectedGraph<T> subGraph = new DirectedGraphImpl<T>();
        for (Edge<T> edge : graph.getEdges()) {
            if (isPath(graph, edge.getSink(), edge.getSource())) {
                subGraph.add(edge);
            }
        }
        return subGraph;
    }

    public List<Cycle<T>> findCycles(DirectedGraph<T> graph) {
        List<Cycle<T>> cycles = new ArrayList<Cycle<T>>();
        for (Edge<T> edge : graph.getEdges()) {
            List<Vertex<T>> path = getPath(graph, edge.getSink(), edge.getSource());
            if (!path.isEmpty()) {
                Cycle<T> cycle = searchCycle(cycles, edge);
                if (cycle == null) {
                    cycle = new Cycle<T>();
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
