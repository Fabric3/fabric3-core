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
package org.fabric3.spi.container.builder.classloader;

/**
 * Implementations receive callbacks when classloaders are provisioned and unprovisioned on participant nodes. Since classloaders correspond to
 * contributions, implementations can use this to receive callbacks when a contribution is activated and deactivated during a deployment.
 * <p/>
 * In a single-VM environment, callbacks will be received when a deployment is made but the classloader will have been provisioned when the
 * contribution was installed.
 */
public interface ClassLoaderListener {

    /**
     * Called when a classloader is deployed.
     *
     * @param classLoader the classloader
     */
    void onDeploy(ClassLoader classLoader);

    /**
     * Called when a classloader is undeployed.
     *
     * @param classLoader the classloader
     */
    void onUndeploy(ClassLoader classLoader);

}
