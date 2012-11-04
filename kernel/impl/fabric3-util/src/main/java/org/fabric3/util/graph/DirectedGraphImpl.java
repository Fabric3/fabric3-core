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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default directed graph implementation.
 */
public class DirectedGraphImpl<T> implements DirectedGraph<T> {
    private Map<Vertex<T>, VertexHolder> graphVertices;
    private Set<Edge<T>> graphEdges;

    public DirectedGraphImpl() {
        graphVertices = new HashMap<Vertex<T>, VertexHolder>();
        graphEdges = new HashSet<Edge<T>>();
    }

    public Set<Vertex<T>> getVertices() {
        return graphVertices.keySet();
    }

    public void add(Vertex<T> vertex) {
        if (graphVertices.containsKey(vertex)) {
            return;
        }
        graphVertices.put(vertex, new VertexHolder());
    }

    public void remove(Vertex<T> vertex) {
        List<Edge<T>> edges = new ArrayList<Edge<T>>(getOutgoingEdges(vertex));
        for (Edge<T> edge : edges) {
            removeEdge(edge);
        }
        graphVertices.remove(vertex);
    }

    public Set<Vertex<T>> getAdjacentVertices(Vertex<T> vertex) {
        Set<Vertex<T>> adjacentVertices = new HashSet<Vertex<T>>();
        Set<Edge<T>> incidentEdges = getOutgoingEdges(vertex);
        if (incidentEdges != null) {
            for (Edge<T> edge : incidentEdges) {
                adjacentVertices.add(edge.getOppositeVertex(vertex));
            }
        }
        return adjacentVertices;
    }

    public List<Vertex<T>> getOutgoingAdjacentVertices(Vertex<T> vertex) {
        return getAdjacentVertices(vertex, true);
    }

    public List<Vertex<T>> getIncomingAdjacentVertices(Vertex<T> vertex) {
        return getAdjacentVertices(vertex, false);
    }

    public Edge<T> getEdge(Vertex<T> source, Vertex<T> sink) {
        Set<Edge<T>> edges = getOutgoingEdges(source);
        for (Edge<T> edge : edges) {
            if (edge.getSink() == sink) {
                return edge;
            }
        }
        return null;
    }

    public Set<Edge<T>> getOutgoingEdges(Vertex<T> vertex) {
        return graphVertices.get(vertex).getOutgoingEdges();
    }

    public Set<Edge<T>> getIncomingEdges(Vertex<T> vertex) {
        return graphVertices.get(vertex).getIncomingEdges();
    }

    public Set<Edge<T>> getEdges() {
        return graphEdges;
    }

    public void add(Edge<T> edge) {
        if (graphEdges.contains(edge)) {
            return;
        }
        Vertex<T> source = edge.getSource();
        Vertex<T> sink = edge.getSink();

        if (!graphVertices.containsKey(source)) {
            add(source);
        }
        if ((sink != source) && !graphVertices.containsKey(sink)) {
            add(sink);
        }
        Set<Edge<T>> sourceEdges = getOutgoingEdges(source);

        sourceEdges.add(edge);
        if (source != sink) {
            // avoid adding the edge a second time if the edge points back on itself
            Set<Edge<T>> sinkEdges = getIncomingEdges(sink);
            sinkEdges.add(edge);
        }

        graphEdges.add(edge);
        VertexHolder sourceHolder = graphVertices.get(edge.getSource());
        VertexHolder sinkHolder = graphVertices.get(edge.getSink());
        sourceHolder.getOutgoingEdges().add(edge);
        sinkHolder.getIncomingEdges().add(edge);
    }

    public void remove(Edge<T> edge) {
        removeEdge(edge);
        Vertex<T> source = edge.getSource();
        Vertex<T> sink = edge.getSink();
        VertexHolder sourceHolder = graphVertices.get(source);
        VertexHolder sinkHolder = graphVertices.get(sink);
        // remove the edge from the source's outgoing edges
        sourceHolder.getOutgoingEdges().remove(edge);
        // remove the edge from the sink's incoming edges
        sinkHolder.getIncomingEdges().remove(edge);
    }

    private void removeEdge(Edge<T> edge) {
        // Remove the edge from the vertices incident edges.
        Vertex<T> source = edge.getSource();
        Set<Edge<T>> sourceEdges = getOutgoingEdges(source);
        sourceEdges.remove(edge);

        Vertex<T> sink = edge.getSink();
        Set<Edge<T>> sinkEdges = getIncomingEdges(sink);
        sinkEdges.remove(edge);

        // Remove the edge from edgeSet
        graphEdges.remove(edge);
    }

    /**
     * Returns the outgoing or incoming adjacent vertices for a given vertex
     *
     * @param vertex   the vertex.
     * @param outGoing true for returning outgoing vertices.
     * @return the adjacent vertices
     */
    private List<Vertex<T>> getAdjacentVertices(Vertex<T> vertex, boolean outGoing) {
        List<Vertex<T>> adjacentVertices = new ArrayList<Vertex<T>>();
        Set<Edge<T>> edges;
        if (outGoing) {
            edges = getOutgoingEdges(vertex);
        } else {
            edges = getIncomingEdges(vertex);
        }
        for (Edge<T> edge : edges) {
            Vertex<T> oppositeVertex = edge.getOppositeVertex(vertex);
            adjacentVertices.add(oppositeVertex);
        }
        return adjacentVertices;
    }

    private class VertexHolder {
        private Set<Edge<T>> incoming = new HashSet<Edge<T>>();
        private Set<Edge<T>> outgoingEdges = new HashSet<Edge<T>>();

        public Set<Edge<T>> getIncomingEdges() {
            return incoming;
        }

        public Set<Edge<T>> getOutgoingEdges() {
            return outgoingEdges;
        }

    }

}

