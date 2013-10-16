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
