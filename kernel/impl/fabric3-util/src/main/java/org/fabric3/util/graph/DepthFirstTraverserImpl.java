/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Default implementation of a depth first search.
 *
 * @version $Rev$ $Date$
 */
public class DepthFirstTraverserImpl<T> implements DepthFirstTraverser<T> {

    public List<Vertex<T>> traverse(DirectedGraph<T> graph, Vertex<T> start) {
        return traverse(graph, start, new TrueVisitor<T>());
    }

    public List<Vertex<T>> traversePath(DirectedGraph<T> graph, Vertex<T> start, Vertex<T> end) {
        TerminatingVisitor<T> visitor = new TerminatingVisitor<T>(end);
        List<Vertex<T>> path = traverse(graph, start, visitor);
        if (visitor.wasFound()) {
            return path;
        }
        return Collections.emptyList();
    }

    private List<Vertex<T>> traverse(DirectedGraph<T> graph, Vertex<T> start, Visitor<T> visitor) {
        List<Vertex<T>> visited = new ArrayList<Vertex<T>>();
        List<Vertex<T>> stack = new ArrayList<Vertex<T>>();
        Set<Vertex<T>> seen = new HashSet<Vertex<T>>(visited);
        stack.add(start);
        seen.add(start);
        do {
            // mark as visited
            Vertex<T> next = stack.remove(stack.size() - 1);
            visited.add(next);
            if (!visitor.visit(next)) {
                return visited;
            }

            // add all non-visited adjacent vertices to the stack
            Set<Vertex<T>> adjacentVertices = graph.getAdjacentVertices(next);
            for (Vertex<T> v : adjacentVertices) {
                seen.add(v);
                stack.add(v);
            }

        } while (!stack.isEmpty());
        return visited;
    }


}
