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
 *
 * Based on org.osgi.framework.Version from the OSGi Alliance under the Apache 2.0 License:
 *
 * Copyright (c) OSGi Alliance (2004, 2007). All Rights Reserved.
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
 *
 */
package org.fabric3.host;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * A version identifier. Version identifiers have four components:
 * <pre>
 *  <ol>
 * <li>Major version. A non-negative integer.</li>
 * <li>Minor version. A non-negative integer.</li>
 * <li>Micro version. A non-negative integer.</li>
 * <li>Qualifier. A text string. </li>
 * </ol>
 * </pre>
 * <p/>
 * This implementation is based on org.osgi.framework.Version from the OSGi Alliance issued under the Apache 2.0 License.
 */
public class Version implements Comparable, Serializable {
    private static final long serialVersionUID = -2755678770473603563L;
    private static final String SEPARATOR = ".";

    private final int major;
    private final int minor;
    private final int micro;
    private final String qualifier;

    /**
     * The empty version "0.0.0". Equivalent to calling <code>new Version(0,0,0)</code>.
     */
    public static final Version emptyVersion = new Version(0, 0, 0);

    /**
     * Creates a version identifier from the specified numerical components.
     * <p/>
     * <p/>
     * The qualifier is set to the empty string.
     *
     * @param major Major component of the version identifier.
     * @param minor Minor component of the version identifier.
     * @param micro Micro component of the version identifier.
     * @throws IllegalArgumentException If the numerical components are negative.
     */
    public Version(int major, int minor, int micro) {
        this(major, minor, micro, null);
    }

    /**
     * Creates a version identifier from the specifed components.
     *
     * @param major     Major component of the version identifier.
     * @param minor     Minor component of the version identifier.
     * @param micro     Micro component of the version identifier.
     * @param qualifier Qualifier component of the version identifier. If <code>null</code> is specified, then the qualifier will be set to the empty
     *                  string.
     * @throws IllegalArgumentException If the numerical components are negative or the qualifier string is invalid.
     */
    public Version(int major, int minor, int micro, String qualifier) {
        if (qualifier == null) {
            qualifier = "";
        }

        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
        validate();
    }

    /**
     * Created a version identifier from the specified string.
     * <p/>
     * <p/>
     * Here is the grammar for version strings.
     * <p/>
     * <pre>
     * version ::= major('.'minor('.'micro('.'qualifier)?)?)?
     * major ::= digit+
     * minor ::= digit+
     * micro ::= digit+
     * qualifier ::= (alpha|digit|'_'|'-')+
     * digit ::= [0..9]
     * alpha ::= [a..zA..Z]
     * </pre>
     * <p/>
     * There must be no whitespace in version.
     *
     * @param version String representation of the version identifier.
     * @throws IllegalArgumentException If <code>version</code> is improperly formatted.
     */
    public Version(String version) {
        int major;
        int minor = 0;
        int micro = 0;
        String qualifier = "";

        try {
            StringTokenizer st = new StringTokenizer(version, SEPARATOR, true);
            major = Integer.parseInt(st.nextToken());

            if (st.hasMoreTokens()) {
                st.nextToken(); // consume delimiter
                minor = Integer.parseInt(st.nextToken());

                if (st.hasMoreTokens()) {
                    st.nextToken(); // consume delimiter
                    micro = Integer.parseInt(st.nextToken());

                    if (st.hasMoreTokens()) {
                        st.nextToken(); // consume delimiter
                        qualifier = st.nextToken();

                        if (st.hasMoreTokens()) {
                            throw new IllegalArgumentException("Invalid format: " + version);
                        }
                    }
                }
            }
        }
        catch (NoSuchElementException e) {
            throw new IllegalArgumentException("invalid format: " + version);
        }

        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = qualifier;
        validate();
    }

    /**
     * Parses a version identifier from the specified string.
     * <p/>
     * See <code>Version(String)</code> for the format of the version string.
     *
     * @param version String representation of the version identifier. Leading and trailing whitespace will be ignored.
     * @return A <code>Version</code> object representing the version identifier. If <code>version</code> is <code>null</code> or the empty string
     *         then <code>emptyVersion</code> will be returned.
     * @throws IllegalArgumentException If <code>version</code> is improperly formatted.
     */
    public static Version parseVersion(String version) {
        if (version == null) {
            return emptyVersion;
        }

        version = version.trim();
        if (version.length() == 0) {
            return emptyVersion;
        }

        return new Version(version);
    }

    /**
     * Returns the major component of this version identifier.
     *
     * @return The major component.
     */
    public int getMajor() {
        return major;
    }

    /**
     * Returns the minor component of this version identifier.
     *
     * @return The minor component.
     */
    public int getMinor() {
        return minor;
    }

    /**
     * Returns the micro component of this version identifier.
     *
     * @return The micro component.
     */
    public int getMicro() {
        return micro;
    }

    /**
     * Returns the qualifier component of this version identifier.
     *
     * @return The qualifier component.
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * Returns the string representation of this version identifier.
     * <p/>
     * <p/>
     * The format of the version string will be <code>major.minor.micro</code> if qualifier is the empty string or
     * <code>major.minor.micro.qualifier</code> otherwise.
     *
     * @return The string representation of this version identifier.
     */
    public String toString() {
        String base = major + SEPARATOR + minor + SEPARATOR + micro;
        if (qualifier.length() == 0) {
            return base;
        } else {
            return base + SEPARATOR + qualifier;
        }
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return An integer which is a hash code value for this object.
     */
    public int hashCode() {
        return (major << 24) + (minor << 16) + (micro << 8) + qualifier.hashCode();
    }

    /**
     * Compares this <code>Version</code> object to another object.
     * <p/>
     * <p/>
     * A version is considered to be <b>equal to </b> another version if the major, minor and micro components are equal and the qualifier component
     * is equal (using <code>String.equals</code>).
     *
     * @param object The <code>Version</code> object to be compared.
     * @return <code>true</code> if <code>object</code> is a <code>Version</code> and is equal to this object; <code>false</code> otherwise.
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof Version)) {
            return false;
        }

        Version other = (Version) object;
        return (major == other.major) && (minor == other.minor) && (micro == other.micro) && qualifier.equals(other.qualifier);
    }

    /**
     * Compares this <code>Version</code> object to another object.
     * <p/>
     * <p/>
     * A version is considered to be <b>less than </b> another version if its major component is less than the other version's major component, or the
     * major components are equal and its minor component is less than the other version's minor component, or the major and minor components are
     * equal and its micro component is less than the other version's micro component, or the major, minor and micro components are equal and it's
     * qualifier component is less than the other version's qualifier component (using <code>String.compareTo</code>).
     * <p/>
     * A version is considered to be <b>equal to</b> another version if the major, minor and micro components are equal and the qualifier component is
     * equal (using <code>String.compareTo</code>).
     *
     * @param object The <code>Version</code> object to be compared.
     * @return A negative integer, zero, or a positive integer if this object is less than, equal to, or greater than the specified
     *         <code>Version</code> object.
     * @throws ClassCastException If the specified object is not a <code>Version</code>.
     */
    public int compareTo(Object object) {
        if (object == this) {
            return 0;
        }

        Version other = (Version) object;

        int result = major - other.major;
        if (result != 0) {
            return result;
        }

        result = minor - other.minor;
        if (result != 0) {
            return result;
        }

        result = micro - other.micro;
        if (result != 0) {
            return result;
        }

        return qualifier.compareTo(other.qualifier);
    }

    /**
     * Called by the Version constructors to validate the version components.
     *
     * @throws IllegalArgumentException If the numerical components are negative or the qualifier string is invalid.
     */
    private void validate() {
        if (major < 0) {
            throw new IllegalArgumentException("negative major");
        }
        if (minor < 0) {
            throw new IllegalArgumentException("negative minor");
        }
        if (micro < 0) {
            throw new IllegalArgumentException("negative micro");
        }
        int length = qualifier.length();
        for (int i = 0; i < length; i++) {
            if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-".indexOf(qualifier.charAt(i)) == -1) {
                throw new IllegalArgumentException("invalid qualifier");
            }
        }
    }


}