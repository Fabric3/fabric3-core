/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
    private List<Export> exports = new ArrayList<Export>();
    private List<Import> imports = new ArrayList<Import>();
    private List<Library> libraries = new ArrayList<Library>();
    private Set<Capability> requiredCapabilities = new HashSet<Capability>();
    private Set<Capability> providedCapabilities = new HashSet<Capability>();
    private List<Deployable> deployables = new ArrayList<Deployable>();
    private List<String> extensionPoints = new ArrayList<String>();
    private List<String> extend = new ArrayList<String>();
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
