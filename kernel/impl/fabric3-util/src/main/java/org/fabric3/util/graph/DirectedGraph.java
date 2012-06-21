/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import java.util.List;
import java.util.Set;

/**
 * A directed graph.
 *
 * @version $Rev$ $Date$
 */
public interface DirectedGraph<T> {

    /**
     * Adds a vertex.
     *
     * @param vertex the vertex to add
     */
    public void add(Vertex<T> vertex);

    /**
     * Removes a vertex. Also removes any associated edges.
     *
     * @param vertex the vertex to remove
     */
    public void remove(Vertex<T> vertex);

    /**
     * Returns the vertices in the graph.
     *
     * @return the vertices in the graph
     */
    public Set<Vertex<T>> getVertices();

    /**
     * Returns the adjacent vertices to the given vertex.
     *
     * @param vertex the vertex to return adjacent vertices for
     * @return the adjacent vertices to the given vertex
     */
    public Set<Vertex<T>> getAdjacentVertices(Vertex<T> vertex);

    /**
     * Returns the adjacent vertices pointed to by outgoing edges for a given vertex.
     *
     * @param vertex the vertex
     * @return the adjacent vertices
     */
    public List<Vertex<T>> getOutgoingAdjacentVertices(Vertex<T> vertex);

    /**
     * Returns the adjacent vertices pointed to by incoming edges for a given vertex.
     *
     * @param vertex the vertex
     * @return the adjacent vertices
     */
    public List<Vertex<T>> getIncomingAdjacentVertices(Vertex<T> vertex);


    /**
     * Adds an edge.
     *
     * @param edge the edge to add
     */
    public void add(Edge<T> edge);

    /**
     * Removes an edge.
     *
     * @param edge the edge to remove
     */
    public void remove(Edge<T> edge);

    /**
     * Returns all edges in the graph.
     *
     * @return all edges in the graph
     */
    public Set<Edge<T>> getEdges();

    /**
     * Returns an edge between the two given vertices.
     *
     * @param source the source vertex
     * @param sink   the sink vertex
     * @return the edge
     */
    public Edge<T> getEdge(Vertex<T> source, Vertex<T> sink);

    /**
     * Returns the outgoing edges for the given vertex
     *
     * @param vertex the vertex to return the outgoing edges for
     * @return the outgoing edges
     */
    public Set<Edge<T>> getOutgoingEdges(Vertex<T> vertex);

    /**
     * Returns the incoming edges for the given vertex
     *
     * @param vertex the vertex to return the incoming edges for
     * @return the outgoing edges
     */
    public Set<Edge<T>> getIncomingEdges(Vertex<T> vertex);

}

