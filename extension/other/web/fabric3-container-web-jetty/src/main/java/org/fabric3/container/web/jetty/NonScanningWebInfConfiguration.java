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
package org.fabric3.container.web.jetty;

import java.util.Collections;
import java.util.List;

import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;

/**
 * Overrides the default Jetty WebInfConfiguration to not scan library JARs.
 */
public class NonScanningWebInfConfiguration extends WebInfConfiguration {
    protected List<Resource> findJars(WebAppContext context) throws Exception {
        return Collections.emptyList();
    }

    public void configure(WebAppContext context) throws Exception {
        // do nothing
    }
}
