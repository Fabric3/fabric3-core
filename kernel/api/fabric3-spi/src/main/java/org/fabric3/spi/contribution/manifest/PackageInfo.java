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
package org.fabric3.spi.contribution.manifest;

import org.fabric3.api.host.Version;
import org.fabric3.spi.model.version.Versionable;

/**
 * Represents Java package information specified in a Java import or export contribution manifest declaration.
 */
public final class PackageInfo extends Versionable {
    private static final long serialVersionUID = 1011714148953772009L;
    private String name;
    private boolean required;
    private String[] packageNames;

    /**
     * Constructor for an import package declaration specifying a version range.
     *
     * @param name         the package name
     * @param minVersion   the minimum version
     * @param minInclusive true if the minimum version is considered inclusive for range matching
     * @param maxVersion   the maximum version
     * @param maxInclusive if the maximum version is considered inclusive for range matching
     * @param required     if package resolution is required
     */
    public PackageInfo(String name,
                       Version minVersion,
                       boolean minInclusive,
                       Version maxVersion,
                       boolean maxInclusive,
                       boolean required) {
        super(minVersion, minInclusive, maxVersion, maxInclusive);
        setName(name);
        this.required = required;
    }

    /**
     * Constructor for an import or export package declaration specifying an exact version.
     *
     * @param name         the package name
     * @param version      the minimum version
     * @param minInclusive true if the minimum version is considered inclusive for range matching
     * @param required     if package resolution is required
     */
    public PackageInfo(String name, Version version, boolean minInclusive, boolean required) {
        super(version, minInclusive);
        setName(name);
        this.required = required;
    }

    /**
     * Constructor for an import or export package declaration.
     *
     * @param name the package name
     */
    public PackageInfo(String name) {
        super();
        setName(name);
        this.required = true;
    }

    /**
     * Constructor for an export package declaration.
     *
     * @param name    the package name
     * @param version the version
     */
    public PackageInfo(String name, Version version) {
        super(version);
        setName(name);
    }

    /**
     * Constructor for an import package declaration specifying if it is required.
     *
     * @param name     the package name
     * @param required if package resolution is required
     */
    public PackageInfo(String name, boolean required) {
        super();
        setName(name);
        this.required = required;
    }

    /**
     * Default constructor.
     */
    public PackageInfo() {
        super();

    }

    /**
     * The package name.
     *
     * @return package name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the package name
     *
     * @param name the package name
     */
    public void setName(String name) {
        this.name = name;
        packageNames = name.split("\\.");
    }

    /**
     * The minimum required version. When no maximum version is specified, the minimum version is interpreted as a specific required version.
     *
     * @return the version
     */
    public Version getMinVersion() {
        return minVersion;
    }

    /**
     * Sets the minimum required version.
     *
     * @param minVersion the minimum required version.
     */
    public void setMinVersion(Version minVersion) {
        this.minVersion = minVersion;
    }

    /**
     * True if the minimum version range is exclusive.
     *
     * @return true if the minimum version range is exclusive.
     */
    public boolean isMinInclusive() {
        return minInclusive;
    }

    /**
     * Sets if the minimum version range is exclusive
     *
     * @param minInclusive true if the minimum version range is exclusive
     */
    public void setMinInclusive(boolean minInclusive) {
        this.minInclusive = minInclusive;
    }

    /**
     * The maximum required version. When no maximum version is specified, the minimum version is interpreted as a specific required version.
     *
     * @return the maximum version or null
     */
    public Version getMaxVersion() {
        return maxVersion;
    }

    /**
     * Sets the maximum required version.
     *
     * @param maxVersion maximum version
     */
    public void setMaxVersion(Version maxVersion) {
        this.maxVersion = maxVersion;
    }


    /**
     * True if the maximum version range is exclusive.
     *
     * @return true if the maximum version range is exclusive.
     */
    public boolean isMaxInclusive() {
        return maxInclusive;
    }


    /**
     * Sets if the maximum version range is exclusive.
     *
     * @param maxInclusive true if the maximum version range is exclusive
     */
    public void setMaxInclusive(boolean maxInclusive) {
        this.maxInclusive = maxInclusive;
    }

    /**
     * Returns true if the package is required.
     *
     * @return true if the package is required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * True if the package is required.
     *
     * @param required true if the package is required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Returns true if this import package matches the specified export package according to OSGi R4 semantics.
     *
     * @param exportPackage the export package
     * @return true if this import package matches the specified export package
     */
    public boolean matches(PackageInfo exportPackage) {
        if (!super.matches(exportPackage.getMinVersion())) {
            return false;
        }
        // match package names
        int i = 0;
        boolean exportWildcard = exportPackage.packageNames[exportPackage.packageNames.length - 1].equals("*");
        if (packageNames.length < exportPackage.packageNames.length && !"*".equals(packageNames[packageNames.length - 1]) && !exportWildcard) {
            return false;
        }
        for (String packageName : exportPackage.packageNames) {
            if ("*".equals(packageName)) {
                // export wild card found, packages match
                return true;
            } else if (packageNames.length - 1 >= i && !packageName.equals(packageNames[i])) {
                return false;
            }
            i++;
            if (packageNames.length - 1 == i && packageNames.length > exportPackage.packageNames.length && !"*".equals(packageNames[i])) {
                return false;
            }
        }
        if (packageNames.length > 0 && "*".equals(packageNames[packageNames.length - 1])) {
            // match case e.g. export: "javax.jms"  import: "javax.jms.*"
            return exportPackage.packageNames.length == packageNames.length - 1;
        }
        return exportPackage.packageNames.length == packageNames.length;
    }


    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (minVersion != null ? minVersion.hashCode() : 0);
        result = 31 * result + (maxVersion != null ? maxVersion.hashCode() : 0);
        result = 31 * result + (required ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("package: " + name);
        if (minVersion != null) {
            builder.append(" Min: ").append(minVersion);
        }
        if (maxVersion != null) {
            builder.append(" Max: ").append(maxVersion);
        }
        builder.append(" Required: ").append(required);
        return builder.toString();
    }
}
