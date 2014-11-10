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
package org.fabric3.runtime.weblogic.boot;

import org.fabric3.api.host.runtime.HiddenPackages;

/**
 *
 */
public final class WebLogicHiddenPackages {
    private static final String[] PACKAGES;

    static {
        String[] hidden = HiddenPackages.getPackages();
        PACKAGES = new String[hidden.length + 2];
        System.arraycopy(hidden, 0, PACKAGES, 0, hidden.length);
        PACKAGES[PACKAGES.length - 2] = "weblogic.xml.saaj.";
        PACKAGES[PACKAGES.length - 1] = "antlr.";
    }

    public static String[] getPackages() {
        return PACKAGES;
    }


}