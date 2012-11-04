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
package org.fabric3.spi.model.version;

import java.io.Serializable;

import org.fabric3.host.Version;

/**
 * A contribution manifest attribute that is used to specify a version range.
 */
public abstract class Versionable implements Serializable {
    private static final long serialVersionUID = -1603295817528974598L;
    private static final Version NON_SPECIFIED = new Version(0, 0, 0);

    protected Version minVersion = NON_SPECIFIED;
    protected Version maxVersion;
    protected boolean minInclusive = true;
    protected boolean maxInclusive = true;

    /**
     * Constructor for specifying a version range.
     *
     * @param minVersion   the minimum version
     * @param minInclusive true if the minimum version is considered inclusive for range matching
     * @param maxVersion   the maximum version
     * @param maxInclusive if the maximum version is considered inclusive for range matching
     */
    public Versionable(Version minVersion,
                       boolean minInclusive,
                       Version maxVersion,
                       boolean maxInclusive) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    /**
     * Constructor for specifying an exact version.
     *
     * @param version      the minimum version
     * @param minInclusive true if the minimum version is considered inclusive for range matching
     */
    public Versionable(Version version, boolean minInclusive) {
        this.minVersion = version;
        this.minInclusive = minInclusive;
    }

    /**
     * Constructor specifying the minimum version.
     *
     * @param version the minimum version.
     */
    public Versionable(Version version) {
        minVersion = version;
    }

    /**
     * Default constructor.
     */
    public Versionable() {
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
     * Returns true if this versionable matches the other version according to OSGi R4 semantics.
     *
     * @param other the version
     * @return true if this import package matches the specified export package
     */
    protected boolean matches(Version other) {
        if (minVersion != null) {
            if (minInclusive) {
                if (minVersion.compareTo(other) > 0) {
                    return false;
                }
            } else {
                if (minVersion.compareTo(other) >= 0) {
                    return false;
                }
            }
        }
        if (maxVersion != null) {
            if (maxInclusive) {
                if (maxVersion.compareTo(other) < 0) {
                    return false;
                }
            } else {
                if (maxVersion.compareTo(other) <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

}
