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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Felix
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.host.util;

import java.util.StringTokenizer;

/**
 * Helper methods for parsing with operating system information.
 */
public final class OSHelper {
    private static final String VERSION_DELIMETER = ".";
    private static final String QUALIFIER_DELIMETER = "-";

    public static String parseProcessor(String value) {
        value = value.toLowerCase();

        if (value.startsWith("x86_64") || value.startsWith("amd64")) {
            return "x86_64";
        } else if (value.startsWith("x86") || value.startsWith("pentium")
                || value.startsWith("i386") || value.startsWith("i486")
                || value.startsWith("i586") || value.startsWith("i686")) {
            return "x86";
        } else if (value.startsWith("68k")) {
            return "68k";
        } else if (value.startsWith("arm")) {
            return "arm";
        } else if (value.startsWith("alpha")) {
            return "alpha";
        } else if (value.startsWith("ignite") || value.startsWith("psc1k")) {
            return "ignite";
        } else if (value.startsWith("mips")) {
            return "mips";
        } else if (value.startsWith("parisc")) {
            return "parisc";
        } else if (value.startsWith("powerpc") || value.startsWith("power")
                || value.startsWith("ppc")) {
            return "powerpc";
        } else if (value.startsWith("sparc")) {
            return "sparc";
        }
        return value;
    }

    public static String parseVersion(String value) {
        int major = 0;
        int minor = 0;
        int micro = 0;
        try {
            StringTokenizer st = new StringTokenizer(value, VERSION_DELIMETER, true);
            major = Integer.parseInt(st.nextToken());

            if (st.hasMoreTokens()) {
                st.nextToken(); // consume delimiter
                minor = Integer.parseInt(st.nextToken());

                if (st.hasMoreTokens()) {
                    st.nextToken(); // consume delimiter
                    String microStr = st.nextToken();
                    if (microStr.indexOf(QUALIFIER_DELIMETER) < 0) {
                        micro = Integer.parseInt(microStr);
                    } else {
                        micro = Integer.parseInt(microStr.substring(0, microStr
                                .indexOf(QUALIFIER_DELIMETER)));
                    }
                }
            }
        } catch (Exception ex) {
            return "0.0.0";
        }

        return major + "." + minor + "." + micro;
    }

}