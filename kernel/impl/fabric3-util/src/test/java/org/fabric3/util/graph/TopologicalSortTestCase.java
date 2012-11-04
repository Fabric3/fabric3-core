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

import junit.framework.TestCase;

/**
 *
 */
public class TopologicalSortTestCase extends TestCase {

    public void testMultiLevelSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<String>("C");
        Edge<String> edgeAC = new EdgeImpl<String>(a, c);
        graph.add(edgeAC);
        Edge<String> edgeBC = new EdgeImpl<String>(b, c);
        graph.add(edgeBC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().sort(graph);
        assertEquals(a, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(c, list.get(2));
    }

    public void testMultiLevelReverseSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<String>("C");
        Edge<String> edgeAC = new EdgeImpl<String>(a, c);
        graph.add(edgeAC);
        Edge<String> edgeBC = new EdgeImpl<String>(b, c);
        graph.add(edgeBC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().reverseSort(graph);
        assertEquals(c, list.get(0));
        assertEquals(b, list.get(1));
        assertEquals(a, list.get(2));
    }

    public void testReverseSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<String>("C");
        Edge<String> edgeAC = new EdgeImpl<String>(a, c);
        graph.add(edgeAC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().reverseSort(graph);
        assertEquals(a, list.get(2));
        assertTrue(list.contains(c));
        assertTrue(list.contains(b));
    }

    public void testSort() throws Exception {
        DirectedGraph<String> graph = new DirectedGraphImpl<String>();
        Vertex<String> a = new VertexImpl<String>("A");
        Vertex<String> b = new VertexImpl<String>("B");
        Edge<String> edgeAB = new EdgeImpl<String>(a, b);
        graph.add(edgeAB);
        Vertex<String> c = new VertexImpl<String>("C");
        Edge<String> edgeAC = new EdgeImpl<String>(a, c);
        graph.add(edgeAC);
        List<Vertex<String>> list = new TopologicalSorterImpl<String>().sort(graph);
        assertEquals(a, list.get(0));
        assertTrue(list.contains(b));
        assertTrue(list.contains(c));
    }

}
