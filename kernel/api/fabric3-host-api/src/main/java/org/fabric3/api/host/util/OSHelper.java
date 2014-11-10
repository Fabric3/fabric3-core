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
 */
package org.fabric3.api.host.util;

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
        int major;
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
                    if (!microStr.contains(QUALIFIER_DELIMETER)) {
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