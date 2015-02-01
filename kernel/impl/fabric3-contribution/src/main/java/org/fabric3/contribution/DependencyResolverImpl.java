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
package org.fabric3.contribution;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.DependencyResolver;
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
import org.oasisopen.sca.annotation.Reference;

/**
 * Orders contribution dependencies by resolving imports and capabilities and then performing a topological sort of the dependency graph.
 */
public class DependencyResolverImpl implements DependencyResolver {
    private CycleDetector<Contribution> detector;
    private TopologicalSorter<Contribution> sorter;
    private MetaDataStore store;

    public DependencyResolverImpl(@Reference MetaDataStore store) {
        this.store = store;
        detector = new CycleDetectorImpl<>();
        sorter = new TopologicalSorterImpl<>();
    }

    public List<Contribution> resolve(List<Contribution> contributions) throws Fabric3Exception {
        DirectedGraph<Contribution> dag = new DirectedGraphImpl<>();
        // add the contributions as vertices
        for (Contribution contribution : contributions) {
            dag.add(new VertexImpl<>(contribution));
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
        DirectedGraph<Contribution> dag = new DirectedGraphImpl<>();
        // add the contributions as vertices
        for (Contribution contribution : contributions) {
            dag.add(new VertexImpl<>(contribution));
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
                            Edge<Contribution> edge = new EdgeImpl<>(source, sink);
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
            List<Contribution> ordered = new ArrayList<>(vertices.size());
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
     * Resolves imports for the contribution represented by the current DAG vertex. Resolution will be performed against contributions loaded previously in the
     * <code>MetaDataStore</code> and against contributions being loaded from the DAG. When an import is resolved by an export from a contribution in the DAG,
     * the later will be updated with an edge from the source contribution vertex to the target contribution vertex.
     *
     * @param source the contribution to resolve imports for
     * @param dag    the current contribution dag
     * @throws Fabric3Exception if a resolution error occurs
     */
    private void resolveImports(Vertex<Contribution> source, DirectedGraph<Contribution> dag) throws Fabric3Exception {
        Contribution contribution = source.getEntity();
        ContributionManifest manifest = contribution.getManifest();
        for (Iterator<Import> iterator = manifest.getImports().iterator(); iterator.hasNext(); ) {
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
     * Resolves an import against other contributions loaded previously in the <code>MetaDataStore</code> and against contributions being loaded from the DAG.
     * When an import is resolved by an export from a contribution in the DAG, the later will be updated with an edge from the source contribution vertex to the
     * target contribution vertex.
     *
     * @param imprt  the import to resolve
     * @param source the contribution to resolve imports for
     * @param dag    the current contribution dag
     * @throws Fabric3Exception if a resolution error occurs
     */
    private void resolveExternalImport(Import imprt, Vertex<Contribution> source, DirectedGraph<Contribution> dag) throws Fabric3Exception {
        // See if the import is already stored. Extension imports do not need to be checked since we assume extensions are installed prior
        Contribution contribution = source.getEntity();
        URI uri = contribution.getUri();
        List<Vertex<Contribution>> sinks = resolveImport(imprt, uri, dag);
        if (sinks.isEmpty()) {
            List<Contribution> resolvedContributions = store.resolve(uri, imprt);
            checkInstalled(contribution, resolvedContributions);
            if (resolvedContributions.isEmpty() && imprt.isRequired()) {
                throw new Fabric3Exception("Unable to resolve import " + imprt + " in " + uri);
            }
        } else {
            for (Vertex<Contribution> sink : sinks) {
                Edge<Contribution> edge = new EdgeImpl<>(source, sink);
                dag.add(edge);
            }
        }
    }

    /**
     * Resolves an import where the contribution also exports the same symbol as the import (e.g. a Java package or qualified name). <p/> The following OSGi
     * resolution algorithm defined in R4 Section 3.1 is followed: <p/> <p/> <strong>External</strong> If the import resolves to an export statement in another
     * bundle, then the overlapping export definition in this contribution is discarded. <p/> <p/> <strong>Internal</strong>  If the import is resolved to an
     * export statement in this module, then the overlapping import definition in this contribution is discarded. <p/> <p/> When an import is resolved by an
     * export from a contribution in the DAG, the later will be updated with an edge from the source contribution vertex to the target contribution vertex.
     *
     * @param imprt    the import to resolve
     * @param iterator the import iterator - used to remove the import from the containing manifest if it is resolved by the export in the same contribution
     * @param source   the source contribution
     * @param dag      the current DAG to resolve against
     * @throws Fabric3Exception if there is a resolution error
     */
    private void resolveOverlappingImport(Import imprt, Iterator<Import> iterator, Vertex<Contribution> source, DirectedGraph<Contribution> dag)
            throws Fabric3Exception {
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
                Edge<Contribution> edge = new EdgeImpl<>(source, sink);
                dag.add(edge);
            }
            //  externally resolved, drop the export
            dropExport(imprt, manifest);
        }
    }

    private void resolveCapabilities(Vertex<Contribution> source, DirectedGraph<Contribution> dag) throws Fabric3Exception {
        Contribution contribution = source.getEntity();
        URI uri = contribution.getUri();
        for (Capability capability : contribution.getManifest().getRequiredCapabilities()) {
            // See if a previously installed contribution supplies the capability
            List<Vertex<Contribution>> sinks = findCapabilityVertices(capability, uri, dag);
            if (sinks.isEmpty()) {
                Set<Contribution> resolvedContributions = store.resolveCapability(capability.getName());
                for (Contribution resolved : resolvedContributions) {
                    if (resolved != null && ContributionState.INSTALLED != resolved.getState()) {
                        throw new Fabric3Exception("Contribution " + contribution.getUri() + " requires a capability provided by " + resolved.getUri()
                                                     + " which is not installed");
                    }
                }
                if (resolvedContributions.isEmpty()) {
                    throw new Fabric3Exception("Unable to resolve capability " + capability + " required by " + uri);
                }

            } else {
                for (Vertex<Contribution> sink : sinks) {
                    Edge<Contribution> edge = new EdgeImpl<>(source, sink);
                    dag.add(edge);
                }
            }
        }
    }

    /**
     * Resolve the import against the graph of contributions being loaded, returning the vertices in the graph with a matching export. Per OSGi, all exports
     * must be scanned to ensure that if a resolved export exists, it is used instead of an unresolved on.
     *
     * @param imprt           the import to resolve
     * @param contributionUri the importing contribution URI
     * @param dag             the graph to resolve against
     * @return the matching Vertex or null
     */
    private List<Vertex<Contribution>> resolveImport(Import imprt, URI contributionUri, DirectedGraph<Contribution> dag) {
        List<Vertex<Contribution>> vertices = new ArrayList<>();

        Map<Vertex<Contribution>, Export> candidates = new LinkedHashMap<>();

        if (!imprt.getResolved().isEmpty()) {
            // already resolved
            for (Map.Entry<URI, Export> entry : imprt.getResolved().entrySet()) {
                for (Vertex<Contribution> vertex : dag.getVertices()) {
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
     * @throws Fabric3Exception if there is an error sorting the DAG
     */
    private List<Contribution> sort(DirectedGraph<Contribution> dag) throws Fabric3Exception {
        // detect cycles
        List<Cycle<Contribution>> cycles = detector.findCycles(dag);
        if (!cycles.isEmpty()) {
            // cycles were detected
            StringBuilder builder = new StringBuilder();
            for (Cycle<Contribution> cycle : cycles) {
                for (Vertex<Contribution> vertex : cycle.getOriginPath()) {
                    builder.append(vertex.getEntity().getUri()).append("\n");
                }
            }
            throw new Fabric3Exception("Cyclic dependencies found:\n" + builder);
        }
        try {
            List<Vertex<Contribution>> vertices = sorter.reverseSort(dag);
            List<Contribution> ordered = new ArrayList<>(vertices.size());
            for (Vertex<Contribution> vertex : vertices) {
                ordered.add(vertex.getEntity());
            }
            return ordered;
        } catch (GraphException e) {
            throw new Fabric3Exception(e);
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
        List<Vertex<Contribution>> vertices = new ArrayList<>();
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
        if (imprt.isMultiplicity()) {
            return; // multiplicity imports do not drop exports
        }
        for (Iterator<Export> iterator = manifest.getExports().iterator(); iterator.hasNext(); ) {
            Export export = iterator.next();
            if (export.match(imprt)) {
                iterator.remove();
                break;
            }
        }
    }

    private void dropImport(Export export, ContributionManifest manifest) {
        for (Iterator<Import> iterator = manifest.getImports().iterator(); iterator.hasNext(); ) {
            Import imprt = iterator.next();
            if (imprt.isMultiplicity()) {
                return; // multiplicity imports do not drop exports
            }
            if (export.match(imprt)) {
                iterator.remove();
                break;
            }
        }
    }

    private void checkInstalled(Contribution contribution, List<Contribution> resolvedContributions) throws Fabric3Exception {
        for (Contribution resolved : resolvedContributions) {
            if (resolved != null && ContributionState.INSTALLED != resolved.getState()) {
                throw new Fabric3Exception("Contribution " + contribution.getUri() + " imports " + resolved.getUri() + " which is not installed");
            }
        }
    }

}
