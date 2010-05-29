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
package org.fabric3.spi.contribution.manifest;

/**
 * Represents Java package information specified in a Java import or export contribution manifest declaration.
 *
 * @version $Rev$ $Date$
 */
public final class PackageInfo {
    private static final PackageVersion NON_SPECIFIED = new PackageVersion(0, 0, 0);
    private String name;
    private PackageVersion minVersion = NON_SPECIFIED;
    private PackageVersion maxVersion;
    private boolean required;
    private String[] packageNames;
    private boolean minInclusive = true;
    private boolean maxInclusive = true;

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
                       PackageVersion minVersion,
                       boolean minInclusive,
                       PackageVersion maxVersion,
                       boolean maxInclusive,
                       boolean required) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        setName(name);
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
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
    public PackageInfo(String name, PackageVersion version, boolean minInclusive, boolean required) {
        setName(name);
        this.minVersion = version;
        this.minInclusive = minInclusive;
        this.required = required;
    }

    /**
     * Constructor for an import or export package declaration.
     *
     * @param name the package name
     */
    public PackageInfo(String name) {
        setName(name);
        this.required = true;
    }

    /**
     * Constructor for an export package declaration.
     *
     * @param name    the package name
     * @param version the version
     */
    public PackageInfo(String name, PackageVersion version) {
        setName(name);
        this.minVersion = version;
    }

    /**
     * Constructor for an import package declaration specifying if it is required.
     *
     * @param name     the package name
     * @param required if package resolution is required
     */
    public PackageInfo(String name, boolean required) {
        setName(name);
        this.required = required;
    }

    /**
     * Default constructor.
     */
    public PackageInfo() {

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
    public PackageVersion getMinVersion() {
        return minVersion;
    }

    /**
     * Sets the minimum required version.
     *
     * @param minVersion the minimum required version.
     */
    public void setMinVersion(PackageVersion minVersion) {
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
    public PackageVersion getMaxVersion() {
        return maxVersion;
    }

    /**
     * Sets the maximum required version.
     *
     * @param maxVersion maximum version
     */
    public void setMaxVersion(PackageVersion maxVersion) {
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
        // match package names
        int i = 0;
        if (packageNames.length < exportPackage.packageNames.length && !"*".equals(packageNames[packageNames.length - 1])) {
            return false;
        }
        for (String packageName : exportPackage.packageNames) {
            if ("*".equals(packageName)) {
                // export wild card found, packages match
                break;
            } else if (packageNames.length - 1 >= i && !packageName.equals(packageNames[i])) {
                return false;
            }
            i++;
            if (packageNames.length - 1 == i && packageNames.length > exportPackage.packageNames.length && !"*".equals(packageNames[i])) {
                return false;
            }
        }
        if (minVersion != null) {
            if (minInclusive) {
                if (minVersion.compareTo(exportPackage.minVersion) > 0) {
                    return false;
                }
            } else {
                if (minVersion.compareTo(exportPackage.minVersion) >= 0) {
                    return false;
                }
            }
        }
        if (maxVersion != null) {
            // The exporting PackageInfo.minVersion is used as the export version. Compare the range against that.
            if (maxInclusive) {
                if (maxVersion.compareTo(exportPackage.minVersion) < 0) {
                    return false;
                }
            } else {
                if (maxVersion.compareTo(exportPackage.minVersion) <= 0) {
                    return false;
                }
            }
        }
        return true;
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
