/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
        Map<Vertex<T>, AtomicInteger> vertexMap = new HashMap<Vertex<T>, AtomicInteger>();
        List<Vertex<T>> roots = new ArrayList<Vertex<T>>();
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
        DepthFirstTraverser<T> dfs = new DepthFirstTraverserImpl<T>();
        Map<Vertex<T>, AtomicInteger> vertexMap = new HashMap<Vertex<T>, AtomicInteger>();
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

        List<Vertex<T>> roots = new ArrayList<Vertex<T>>();
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
        List<Vertex<T>> visited = new ArrayList<Vertex<T>>();
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
