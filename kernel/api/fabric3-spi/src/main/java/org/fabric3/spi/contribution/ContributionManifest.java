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
package org.fabric3.spi.contribution;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.fabric3.api.host.contribution.Deployable;
import org.fabric3.spi.model.os.Library;

/**
 * A contribution manifest.
 */
public class ContributionManifest implements Serializable {
    private static final long serialVersionUID = -4968254313720890686L;
    private String description;
    private boolean extension;
    private String context;
    private List<Export> exports = new ArrayList<>();
    private List<Import> imports = new ArrayList<>();
    private List<Library> libraries = new ArrayList<>();
    private Set<Capability> requiredCapabilities = new HashSet<>();
    private Set<Capability> providedCapabilities = new HashSet<>();
    private List<Deployable> deployables = new ArrayList<>();
    private List<String> extensionPoints = new ArrayList<>();
    private List<String> extend = new ArrayList<>();
    private List<Pattern> scanExcludes = Collections.emptyList();

    /**
     * Returns the contribution description.
     *
     * @return the contribution description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the contribution description
     *
     * @param description the contribution description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns true if the contribution is an extension.
     *
     * @return true if the contribution is an extension
     */
    public boolean isExtension() {
        return extension;
    }

    /**
     * Sets if the contribution is an extension.
     *
     * @param extension true if the contribution is an extension
     */
    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    /**
     * Returns the contribution context path.
     *
     * @return the contribution context path
     */
    public String getContext() {
        return context;
    }

    /**
     * Sets the contribution context path.
     *
     * @param context the contribution context path
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * Returns the contribution exports.
     *
     * @return the contribution exports
     */
    public List<Export> getExports() {
        return exports;
    }

    /**
     * Adds a contribution export.
     *
     * @param export the contribution export
     */
    public void addExport(Export export) {
        exports.add(export);
    }

    /**
     * Returns the contribution imports.
     *
     * @return the contribution imports
     */
    public List<Import> getImports() {
        return imports;
    }

    /**
     * Adds a contribution import.
     *
     * @param imprt the contribution import
     */
    public void addImport(Import imprt) {
        imports.add(imprt);
    }

    /**
     * Returns native code libraries contained in the contribution.
     *
     * @return native code libraries contained in the contribution
     */
    public List<Library> getLibraries() {
        return libraries;
    }

    /**
     * Adds a native code library configuration.
     *
     * @param library the native code library configuration
     */
    public void addLibrary(Library library) {
        libraries.add(library);
    }

    /**
     * Adds a capability required by the contribution.
     *
     * @param capability a capability required by the contribution
     */
    public void addRequiredCapability(Capability capability) {
        requiredCapabilities.add(capability);
    }

    /**
     * Returns a list of capabilities required by this contribution.
     *
     * @return a list of capabilities required by this contribution
     */
    public Set<Capability> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    /**
     * Adds a capability provided by this contribution.
     *
     * @param capability a capability provided by this contribution
     */
    public void addProvidedCapability(Capability capability) {
        providedCapabilities.add(capability);
    }

    /**
     * Returns a list of capabilities provided by this contribution.
     *
     * @return a list of capabilities provided by this contribution
     */
    public Set<Capability> getProvidedCapabilities() {
        return providedCapabilities;
    }

    /**
     * Returns the list of extension points provided by this contribution.
     *
     * @return the list of extension points provided by this contribution
     */
    public List<String> getExtensionPoints() {
        return extensionPoints;
    }

    /**
     * Adds an extension point provided by this contribution.
     *
     * @param name the extension point  name
     */
    public void addExtensionPoint(String name) {
        extensionPoints.add(name);
    }

    /**
     * Returns the extension points this contribution extends.
     *
     * @return the extension points this contribution extends
     */
    public List<String> getExtends() {
        return extend;
    }

    /**
     * Adds the name of an extension point this contribution extends.
     *
     * @param name the extension point  name
     */
    public void addExtend(String name) {
        extend.add(name);
    }

    /**
     * Returns the contribution deployables.
     *
     * @return the contribution deployables
     */

    public List<Deployable> getDeployables() {
        return deployables;
    }

    /**
     * Adds a contribution deployable.
     *
     * @param deployable the contribution deployable
     */
    public void addDeployable(Deployable deployable) {
        deployables.add(deployable);
    }

    /**
     * Returns file and directory patterns to exclude when the contribution is scanned.
     *
     * @return file and directory patterns to exclude when the contribution is scanned
     */
    public List<Pattern> getScanExcludes() {
        return scanExcludes;
    }

    /**
     * Sets file and directory patterns to exclude when the contribution is scanned.
     *
     * @param excludes file and directory patterns to exclude when the contribution is scanned
     */
    public void setScanExcludes(List<Pattern> excludes) {
        this.scanExcludes = excludes;
    }
}
