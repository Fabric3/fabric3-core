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
package org.fabric3.spi.model.version;

import java.io.Serializable;

import org.fabric3.api.host.Version;

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
