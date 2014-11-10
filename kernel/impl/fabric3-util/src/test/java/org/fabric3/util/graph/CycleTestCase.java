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

import java.util.Set;

import junit.framework.TestCase;

/**
 *
 */
public class CycleTestCase extends TestCase {

    public void testCycle() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<>();
        Vertex<String> a = new VertexImpl<>("A");
        Vertex<String> b = new VertexImpl<>("B");
        Edge<String> edgeAB = new EdgeImpl<>(a, b);
        graph.add(edgeAB);
        Edge<String> edgeBA = new EdgeImpl<>(b, a);
        graph.add(edgeBA);
        CycleDetector<String> detector = new CycleDetectorImpl<>();
        assertTrue(detector.hasCycles(graph));
        DirectedGraph<String> dg = detector.findCycleSubgraph(graph);
        Set<Edge<String>> edges = dg.getEdges();
        assertTrue(edges.contains(edgeAB));
        assertTrue(edges.contains(edgeBA));
    }
}
