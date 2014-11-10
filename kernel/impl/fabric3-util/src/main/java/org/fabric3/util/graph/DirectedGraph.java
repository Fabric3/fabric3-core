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
import java.util.Set;

/**
 * A directed graph.
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

