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
package org.fabric3.contribution;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.util.graph.Cycle;
import org.fabric3.util.graph.CycleDetector;
import org.fabric3.util.graph.CycleDetectorImpl;
import org.fabric3.util.graph.DirectedGraph;
import org.fabric3.util.graph.DirectedGraphImpl;
import org.fabric3.util.graph.Edge;
import org.fabric3.util.graph.EdgeImpl;
import org.fabric3.util.graph.GraphException;
import org.fabric3.util.graph.TopologicalSorter;
import org.fabric3.util.graph.TopologicalSorterImpl;
import org.fabric3.util.graph.Vertex;
import org.fabric3.util.graph.VertexImpl;

/**
 * Default implementation of the DependencyService
 *
 * @version $Rev$ $Date$
 */
public class DependencyServiceImpl implements DependencyService {
    private CycleDetector<Contribution> detector;
    private TopologicalSorter<Contribution> sorter;
    private MetaDataStore store;


    public DependencyServiceImpl(@Reference MetaDataStore store) {
        this.store = store;
        detector = new CycleDetectorImpl<Contribution>();
        sorter = new TopologicalSorterImpl<Contribution>();
    }

    public List<Contribution> order(List<Contribution> contributions) throws DependencyException {
        // create a DAG
        DirectedGraph<Contribution> dag = new DirectedGraphImpl<Contribution>();
        // add the contributions as vertices
        for (Contribution contribution : contributions) {
            dag.add(new VertexImpl<Contribution>(contribution));
        }
        // add edges based on imports
        for (Vertex<Contribution> source : dag.getVertices()) {
            Contribution contribution = source.getEntity();
            ContributionManifest manifest = contribution.getManifest();
            URI uri = contribution.getUri();
            for (Import imprt : manifest.getImports()) {
                // See if the import is already stored
                // note that extension imports do not need to be checked since we assume extensons are installed prior
                Vertex<Contribution> sink = findTargetVertex(dag, uri, imprt);
                if (sink == null) {
                    Contribution resolved = store.resolve(uri, imprt);
                    if (resolved != null && ContributionState.INSTALLED != resolved.getState()) {
                        throw new DependencyException("Contribution " + contribution.getUri() + " imports "
                                + resolved.getUri() + " which is not installed");
                    }
                    if (resolved == null) {
                        throw new UnresolvableImportException("Unable to resolve import " + imprt + " in " + uri, imprt);
                    }

                } else {
                    Edge<Contribution> edge = new EdgeImpl<Contribution>(source, sink);
                    dag.add(edge);
                }
            }

        }
        // detect cycles
        List<Cycle<Contribution>> cycles = detector.findCycles(dag);
        if (!cycles.isEmpty()) {
            // cycles were detected
            throw new CyclicDependencyException(cycles);
        }
        try {
            List<Vertex<Contribution>> vertices = sorter.reverseSort(dag);
            List<Contribution> ordered = new ArrayList<Contribution>(vertices.size());
            for (Vertex<Contribution> vertex : vertices) {
                ordered.add(vertex.getEntity());
            }
            return ordered;
        } catch (GraphException e) {
            throw new DependencyException(e);
        }
    }

    public List<Contribution> orderForUninstall(List<Contribution> contributions) {
        // create a DAG
        DirectedGraph<Contribution> dag = new DirectedGraphImpl<Contribution>();
        // add the contributions as vertices
        for (Contribution contribution : contributions) {
            dag.add(new VertexImpl<Contribution>(contribution));
        }
        // add edges based on imports
        for (Vertex<Contribution> source : dag.getVertices()) {
            Contribution contribution = source.getEntity();
            URI uri = contribution.getUri();
            for (ContributionWire<?, ?> wire : contribution.getWires()) {
                for (Contribution entry : contributions) {
                    if (entry.getUri().equals(wire.getExportContributionUri())) {
                        Import imprt = wire.getImport();
                        Vertex<Contribution> sink = findTargetVertex(dag, uri, imprt);
                        if (sink == null) {
                            // this should not happen
                            throw new AssertionError("Unable to resolve import " + imprt + " in " + uri);
                        }
                        Edge<Contribution> edge = new EdgeImpl<Contribution>(source, sink);
                        dag.add(edge);
                        break;
                    }
                }
            }
        }
        // detect cycles
        List<Cycle<Contribution>> cycles = detector.findCycles(dag);
        if (!cycles.isEmpty()) {
            // this is a programmin error
            throw new AssertionError("Cylces detected");
        }
        try {
            List<Vertex<Contribution>> vertices = sorter.sort(dag);
            List<Contribution> ordered = new ArrayList<Contribution>(vertices.size());
            for (Vertex<Contribution> vertex : vertices) {
                ordered.add(vertex.getEntity());
            }
            return ordered;
        } catch (GraphException e) {
            // this is a programmin error
            throw new AssertionError(e);
        }
    }

    /**
     * Finds the Vertex in the graph with a maching export
     *
     * @param dag             the graph to resolve against
     * @param contributionUri the importing contribution URI
     * @param imprt           the import to resolve
     * @return the matching Vertext or null
     */
    private Vertex<Contribution> findTargetVertex(DirectedGraph<Contribution> dag, URI contributionUri, Import imprt) {
        for (Vertex<Contribution> vertex : dag.getVertices()) {
            Contribution contribution = vertex.getEntity();
            ContributionManifest manifest = contribution.getManifest();
            assert manifest != null;
            for (Export export : manifest.getExports()) {
                // also compare the contribution URI to avoid resolving to a contribution that imports and exports the same namespace
                if (Export.EXACT_MATCH == export.match(imprt) && !contributionUri.equals(contribution.getUri())) {
                    return vertex;
                }
            }
        }
        return null;
    }

}
