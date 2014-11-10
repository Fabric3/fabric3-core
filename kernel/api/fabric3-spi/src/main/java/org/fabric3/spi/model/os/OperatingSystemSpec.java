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
package org.fabric3.spi.model.os;

import org.fabric3.api.host.Version;
import org.fabric3.api.host.os.OperatingSystem;
import org.fabric3.spi.model.version.Versionable;

/**
 * An operating system specification for a native library entry in a contribution manifest.
 */
public class OperatingSystemSpec extends Versionable {
    private static final long serialVersionUID = 464100854160609807L;

    private String name;
    private String processor;

    public OperatingSystemSpec(String name, String processor) {
        this.name = name;
        this.processor = processor;
    }

    public OperatingSystemSpec(String name, String processor, Version minVersion, boolean minInclusive) {
        super(minVersion, minInclusive);
        this.name = name;
        this.processor = processor;
    }

    public OperatingSystemSpec(String name, String processor, Version minVersion, boolean minInclusive, Version maxVersion, boolean maxInclusive) {
        super(minVersion, minInclusive, maxVersion, maxInclusive);
        this.name = name;
        this.processor = processor;
    }

    /**
     * Returns the OS name.
     *
     * @return the OS name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the OS processor architecture or null if not specified.
     *
     * @return the OS processor architecture or null if not specified
     */
    public String getProcessor() {
        return processor;
    }

    /**
     * Returns true if the other OperatingSystemSpec matches the constraints of the present spec.
     *
     * @param other the other OperatingSystemSpec
     * @return true if the other OperatingSystemSpec matches the constraints of the present spec
     */
    public boolean matches(OperatingSystemSpec other) {
        return !(!super.matches(other.getMinVersion()) || !name.equals(other.getName()))
                && (processor == null && other.getProcessor() == null || processor.equals(other.getProcessor()));
    }

    /**
     * Returns true if the OperatingSystem matches the constraints of the present spec.
     *
     * @param os the OperatingSystem
     * @return true if the other OperatingSystem matches the constraints of the present spec
     */
    public boolean matches(OperatingSystem os) {
        if (name.equals(os.getName()) || (name.equalsIgnoreCase("windows") && os.getName().startsWith("Windows"))) {
            if (processor == null || processor.equalsIgnoreCase(os.getProcessor())) {
                return super.matches(os.getVersion());
            }

        }
        return false;
    }


    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (processor != null ? processor.hashCode() : 0);
        result = 31 * result + (minVersion != null ? minVersion.hashCode() : 0);
        result = 31 * result + (maxVersion != null ? maxVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("operating system: " + name);
        if (processor != null) {
            builder.append("processor: ").append(processor);
        }
        if (minVersion != null) {
            builder.append(" Min: ").append(minVersion);
        }
        if (maxVersion != null) {
            builder.append(" Max: ").append(maxVersion);
        }
        return builder.toString();
    }


}
