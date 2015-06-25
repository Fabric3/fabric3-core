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
package org.fabric3.spi.contribution;

import java.net.URL;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Constructs a classpath based on the contents of a contribution. Implementations introspect archives and place any required artifacts on the classpath. For
 * example, a jar processor may place libraries found in /META-INF/lib on the classpath.
 */
public interface ClasspathProcessor {

    /**
     * Returns true if the processor can introspect the given contribution
     *
     * @param contribution the contribution
     * @return true if the processor can introspect the contribution
     */
    boolean canProcess(Contribution contribution);

    /**
     * Constructs the classpath by introspecting the contribution
     *
     * @param contribution the contribution
     * @return the classpath URLs for the given contribution
     * @throws Fabric3Exception if an error occurs during introspection
     */
    List<URL> process(Contribution contribution) throws Fabric3Exception;

}
