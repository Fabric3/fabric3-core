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
package org.fabric3.plugin.api.runtime;

import javax.xml.namespace.QName;
import java.net.URL;

import org.fabric3.api.host.contribution.ContributionException;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.runtime.Fabric3Runtime;

/**
 * A runtime embedded in a build system plugin.
 */
public interface PluginRuntime extends Fabric3Runtime {

    PluginHostInfo getHostInfo();

    /**
     * Deploys a composite by qualified name contained in the Maven module the runtime is currently executing for.
     *
     * @param base      the module output directory location
     * @param composite the composite qualified name to activate
     * @throws ContributionException if a contribution is thrown. The cause may a ValidationException resulting from  errors in the contribution. In this case
     *                               the errors should be reported back to the user.
     * @throws ContainerException    if there is an error activating the test composite
     */
    void deploy(URL base, QName composite) throws ContributionException, ContainerException;

}
