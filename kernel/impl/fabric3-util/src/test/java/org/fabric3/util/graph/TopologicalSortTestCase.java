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

import junit.framework.TestCase;

/**
 *
 */
public class TopologicalSortTestCase extends TestCase {

    public void testMultiLevelSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<>();
        Vertex<String> a = new VertexImpl<>("A");
        Vertex<String> b = new VertexImpl<>("B");
        Edge<String> edgeAB = new EdgeImpl<>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<>("C");
        Edge<String> edgeAC = new EdgeImpl<>(a, c);
        graph.add(edgeAC);
        Edge<String> edgeBC = new EdgeImpl<>(b, c);
        graph.add(edgeBC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().sort(graph);
        assertEquals(a, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(c, list.get(2));
    }

    public void testMultiLevelReverseSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<>();
        Vertex<String> a = new VertexImpl<>("A");
        Vertex<String> b = new VertexImpl<>("B");
        Edge<String> edgeAB = new EdgeImpl<>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<>("C");
        Edge<String> edgeAC = new EdgeImpl<>(a, c);
        graph.add(edgeAC);
        Edge<String> edgeBC = new EdgeImpl<>(b, c);
        graph.add(edgeBC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().reverseSort(graph);
        assertEquals(c, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(a, list.get(2));
    }

    public void testReverseSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<>();
        Vertex<String> a = new VertexImpl<>("A");
        Vertex<String> b = new VertexImpl<>("B");
        Edge<String> edgeAB = new EdgeImpl<>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<>("C");
        Edge<String> edgeAC = new EdgeImpl<>(a, c);
        graph.add(edgeAC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().reverseSort(graph);
        assertEquals(a, list.get(2));
        assertTrue(list.contains(c));
        assertTrue(list.contains(b));
    }

    public void testSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<>();
        Vertex<String> a = new VertexImpl<>("A");
        Vertex<String> b = new VertexImpl<>("B");
        Edge<String> edgeAB = new EdgeImpl<>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<>("C");
        Edge<String> edgeAC = new EdgeImpl<>(a, c);
        graph.add(edgeAC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().sort(graph);
        assertEquals(a, list.get(0));
        assertTrue(list.contains(b));
        assertTrue(list.contains(c));
    }

}
