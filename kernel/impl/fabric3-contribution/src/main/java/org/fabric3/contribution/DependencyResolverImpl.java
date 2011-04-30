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
package org.fabric3.contribution;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.Capability;
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
 * Orders contribution dependencies by resolving imports and capabilities and then performing a topological sort of the dependency graph.
 *
 * @version $Rev$ $Date$
 */
public class DependencyResolverImpl implements DependencyResolver {
    private CycleDetector<Contribution> detector;
    private TopologicalSorter<Contribution> sorter;
    private MetaDataStore store;


    public DependencyResolverImpl(@Reference MetaDataStore store) {
        this.store = store;
        detector = new CycleDetectorImpl<Contribution>();
        sorter = new TopologicalSorterImpl<Contribution>();
    }

    public List<Contribution> resolve(List<Contribution> contributions) throws DependencyException {
        DirectedGraph<Contribution> dag = new DirectedGraphImpl<Contribution>();
        // add the contributions as vertices
        for (Contribution contribution : contributions) {
            dag.add(new VertexImpl<Contribution>(contribution));
        }

        // add edges based on imports and capabilities
        for (Vertex<Contribution> source : dag.getVertices()) {
            resolveImports(source, dag);
            resolveCapabilities(source, dag);
        }

        // detect cycles
        return sort(dag);
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
                        List<Vertex<Contribution>> sinks = resolveImport(imprt, uri, dag);
                        if (sinks.isEmpty()) {
                            // this should not happen
                            throw new AssertionError("Unable to resolve import " + imprt + " in " + uri);
                        }
                        for (Vertex<Contribution> sink : sinks) {
                            Edge<Contribution> edge = new EdgeImpl<Contribution>(source, sink);
                            dag.add(edge);
                        }
                        break;
                    }
                }
            }
        }
        // detect cycles
        List<Cycle<Contribution>> cycles = detector.findCycles(dag);
        if (!cycles.isEmpty()) {
            // this is a programming error
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
            // this is a programming error
            throw new AssertionError(e);
        }
    }

    /**
     * Resolves imports for the contribution represented by the current DAG vertex. Resolution will be performed against contributions loaded
     * previously in the <code>MetaDataStore</code> and against contributions being loaded from the DAG. When an import is resolved by an export from
     * a contribution in the DAG, the later will be updated with an edge from the source contribution vertex to the target contribution vertex.
     *
     * @param source the contribution to resolve imports for
     * @param dag    the current contribution dag
     * @throws DependencyException if a resolution error occurs
     */
    private void resolveImports(Vertex<Contribution> source, DirectedGraph<Contribution> dag) throws DependencyException {
        Contribution contribution = source.getEntity();
        ContributionManifest manifest = contribution.getManifest();
        for (Iterator<Import> iterator = manifest.getImports().iterator(); iterator.hasNext();) {
            Import imprt = iterator.next();
            boolean hasExport = hasMatchingExport(contribution, imprt);
            if (hasExport) {
                resolveOverlappingImport(imprt, iterator, source, dag);
            } else {
                resolveExternalImport(imprt, source, dag);

            }
        }
    }

    /**
     * Resolves an import against other contributions loaded previously in the <code>MetaDataStore</code> and against contributions being loaded from
     * the DAG. When an import is resolved by an export from a contribution in the DAG, the later will be updated with an edge from the source
     * contribution vertex to the target contribution vertex.
     *
     * @param imprt  the import to resolve
     * @param source the contribution to resolve imports for
     * @param dag    the current contribution dag
     * @throws DependencyException if a resolution error occurs
     */
    private void resolveExternalImport(Import imprt, Vertex<Contribution> source, DirectedGraph<Contribution> dag) throws DependencyException {
        // See if the import is already stored. Extension imports do not need to be checked since we assume extensions are installed prior
        Contribution contribution = source.getEntity();
        URI uri = contribution.getUri();
        List<Vertex<Contribution>> sinks = resolveImport(imprt, uri, dag);
        if (sinks.isEmpty()) {
            List<Contribution> resolvedContributions = store.resolve(uri, imprt);
            checkInstalled(contribution, resolvedContributions);
            if (resolvedContributions.isEmpty() && imprt.isRequired()) {
                throw new UnresolvableImportException("Unable to resolve import " + imprt + " in " + uri, imprt);
            }
        } else {
            for (Vertex<Contribution> sink : sinks) {
                Edge<Contribution> edge = new EdgeImpl<Contribution>(source, sink);
                dag.add(edge);
            }
        }
    }

    /**
     * Resolves an import where the contribution also exports the same symbol as the import (e.g. a Java package or qualified name).
     * <p/>
     * The following OSGi resolution algorithm defined in R4 Section 3.1 is followed:
     * <p/>
     * <p/>
     * <strong>External</strong> Ð If the import resolves to an export statement in another bundle, then the overlapping export definition in this
     * contribution is discarded.
     * <p/>
     * <p/>
     * <strong>Internal</strong> Ð If the import is resolved to an export statement in this module, then the overlapping import definition in this
     * contribution is discarded.
     * <p/>
     * <p/>
     * When an import is resolved by an export from a contribution in the DAG, the later will be updated with an edge from the source contribution
     * vertex to the target contribution vertex.
     *
     * @param imprt    the import to resolve
     * @param iterator the import iterator - used to remove the import from the containing manifest if it is resolved by the export in the same
     *                 contribution
     * @param source   the source contribution
     * @param dag      the current DAG to resolve against
     * @throws DependencyException if there is a resolution error
     */
    private void resolveOverlappingImport(Import imprt, Iterator<Import> iterator, Vertex<Contribution> source, DirectedGraph<Contribution> dag)
            throws DependencyException {
        Contribution contribution = source.getEntity();
        ContributionManifest manifest = contribution.getManifest();
        URI uri = contribution.getUri();

        List<Vertex<Contribution>> sinks = resolveImport(imprt, uri, dag);
        if (sinks.isEmpty()) {
            List<Contribution> resolvedContributions = store.resolve(uri, imprt);
            checkInstalled(contribution, resolvedContributions);
            if (resolvedContributions.isEmpty()) {
                // no external exports are found, i.e. internally resolved, drop the import 
                iterator.remove();
            } else {
                //  externally resolved, drop the export
                dropExport(imprt, manifest);
            }
        } else {
            for (Vertex<Contribution> sink : sinks) {
                Edge<Contribution> edge = new EdgeImpl<Contribution>(source, sink);
                dag.add(edge);
            }
            //  externally resolved, drop the export
            dropExport(imprt, manifest);
        }
    }

    private void resolveCapabilities(Vertex<Contribution> source, DirectedGraph<Contribution> dag) throws DependencyException {
        Contribution contribution = source.getEntity();
        URI uri = contribution.getUri();
        for (Capability capability : contribution.getManifest().getRequiredCapabilities()) {
            // See if a previously installed contribution supplies the capability
            List<Vertex<Contribution>> sinks = findCapabilityVertices(capability, uri, dag);
            if (sinks.isEmpty()) {
                Set<Contribution> resolvedContributions = store.resolveCapability(capability.getName());
                for (Contribution resolved : resolvedContributions) {
                    if (resolved != null && ContributionState.INSTALLED != resolved.getState()) {
                        throw new DependencyException("Contribution " + contribution.getUri() + " requires a capability provided by "
                                + resolved.getUri() + " which is not installed");
                    }
                }
                if (resolvedContributions.isEmpty()) {
                    throw new UnresolvableCapabilityException("Unable to resolve capability " + capability + " required by " + uri);
                }

            } else {
                for (Vertex<Contribution> sink : sinks) {
                    Edge<Contribution> edge = new EdgeImpl<Contribution>(source, sink);
                    dag.add(edge);
                }
            }
        }
    }

    /**
     * Resolve the import against the graph of contributions being loaded, returning the vertices in the graph with a matching export. Per OSGi, all
     * exports must be scanned to ensure that if a resolved export exists, it is used instead of an unresolved on.
     *
     * @param imprt           the import to resolve
     * @param contributionUri the importing contribution URI
     * @param dag             the graph to resolve against
     * @return the matching Vertex or null
     */
    private List<Vertex<Contribution>> resolveImport(Import imprt, URI contributionUri, DirectedGraph<Contribution> dag) {
        List<Vertex<Contribution>> vertices = new ArrayList<Vertex<Contribution>>();

        Map<Vertex<Contribution>, Export> candidates = new LinkedHashMap<Vertex<Contribution>, Export>();

        if (!imprt.getResolved().isEmpty()) {
            // already resolved
            for (Map.Entry<URI, Export> entry : imprt.getResolved().entrySet()) {
                for (Vertex<Contribution> vertex : vertices) {
                    if (vertex.getEntity().getUri().equals(entry.getKey())) {
                        vertices.add(vertex);
                        break;
                    }
                }
            }
            return vertices;
        }

        for (Vertex<Contribution> vertex : dag.getVertices()) {
            Contribution contribution = vertex.getEntity();
            ContributionManifest manifest = contribution.getManifest();
            URI location = imprt.getLocation();
            for (Export export : manifest.getExports()) {
                URI exportUri = contribution.getUri();
                // also compare the contribution URI to avoid resolving to a contribution that imports and exports the same namespace
                if (export.match(imprt) && !contributionUri.equals(contribution.getUri())) {
                    if (location != null) {
                        // explicit location specified
                        if (location.equals(exportUri)) {
                            vertices.add(vertex);
                            imprt.addResolved(exportUri, export);
                            export.resolve();
                            dropImport(export, manifest);
                            return vertices; // done since explicit locations must resolve to one contribution
                        }
                    } else {
                        if (!imprt.isMultiplicity() && export.isResolved()) {
                            // single value to resolve - return the vertex since the export matches and is already resolved for an import
                            vertices.add(vertex);
                            imprt.addResolved(exportUri, export);
                            return vertices;
                        }
                        // export matches, track it in the candidates
                        candidates.put(vertex, export);
                        // multiplicity, check other vertices
                        break;
                    }
                }
            }
        }

        // first pass over candidates - select previously resolved exports
        for (Map.Entry<Vertex<Contribution>, Export> entry : candidates.entrySet()) {
            Vertex<Contribution> vertex = entry.getKey();
            Export export = entry.getValue();
            if (export.isResolved()) {
                vertices.add(vertex);
                imprt.addResolved(vertex.getEntity().getUri(), export);
                if (!imprt.isMultiplicity()) {
                    // not a multiplicity, return the resolved export
                    return vertices;
                }
            }
        }

        // there were no resolved exports. loop through and select all exports for a multiplicity or the first export
        if (vertices.isEmpty() && !candidates.isEmpty()) {
            for (Map.Entry<Vertex<Contribution>, Export> entry : candidates.entrySet()) {
                Vertex<Contribution> vertex = entry.getKey();
                Export export = entry.getValue();
                vertices.add(vertex);
                imprt.addResolved(vertex.getEntity().getUri(), export);
                export.resolve();
                dropImport(export, vertex.getEntity().getManifest());
                if (!imprt.isMultiplicity()) {
                    return vertices;
                }

            }

        }
        return vertices;
    }

    /**
     * Returns true if the import has a matching export on the contribution.
     *
     * @param contribution the contribution
     * @param imprt        the import
     * @return true if the import has a matching export on the contribution
     */
    private boolean hasMatchingExport(Contribution contribution, Import imprt) {
        for (Export export : contribution.getManifest().getExports()) {
            if (export.match(imprt)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sorts the DAG topologically, ordering dependencies first.
     *
     * @param dag the DAG
     * @return the sorted contributions
     * @throws DependencyException if there is an error sorting the DAG
     */
    private List<Contribution> sort(DirectedGraph<Contribution> dag) throws DependencyException {
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

    /**
     * Finds vertices in the graph providing the given capability.
     *
     * @param capability      the capability
     * @param contributionUri the current contribution URI; used to avoid creating a cycle
     * @param dag             the graph
     * @return the vertices
     */
    private List<Vertex<Contribution>> findCapabilityVertices(Capability capability, URI contributionUri, DirectedGraph<Contribution> dag) {
        List<Vertex<Contribution>> vertices = new ArrayList<Vertex<Contribution>>();
        for (Vertex<Contribution> vertex : dag.getVertices()) {
            Contribution contribution = vertex.getEntity();
            if (contribution.getManifest().getProvidedCapabilities().contains(capability) && !contributionUri.equals(contribution.getUri())) {
                vertices.add(vertex);
                break;
            }
        }
        return vertices;
    }

    private void dropExport(Import imprt, ContributionManifest manifest) {
        for (Iterator<Export> iterator = manifest.getExports().iterator(); iterator.hasNext();) {
            Export export = iterator.next();
            if (export.match(imprt)) {
                iterator.remove();
                break;
            }
        }
    }

    private void dropImport(Export export, ContributionManifest manifest) {
        for (Iterator<Import> iterator = manifest.getImports().iterator(); iterator.hasNext();) {
            Import imprt = iterator.next();
            if (export.match(imprt)) {
                iterator.remove();
                break;
            }
        }
    }

    private void checkInstalled(Contribution contribution, List<Contribution> resolvedContributions) throws DependencyException {
        for (Contribution resolved : resolvedContributions) {
            if (resolved != null && ContributionState.INSTALLED != resolved.getState()) {
                throw new DependencyException("Contribution " + contribution.getUri() + " imports "
                        + resolved.getUri() + " which is not installed");
            }
        }
    }


}
