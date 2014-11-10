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
package org.fabric3.node.domain;

import java.net.URI;

import org.fabric3.api.host.Names;
import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 *
 */
public class ContributionResolver {

    /**
     * Returns the contribution URI for the type. In non-managed environments, {@link Names#HOST_CONTRIBUTION} will be returned.
     *
     * @param type the type
     * @return the contribution URI.
     */
    public static URI getContribution(Class<?> type) {
        if (Thread.currentThread().getContextClassLoader() instanceof MultiParentClassLoader) {
            return ((MultiParentClassLoader) Thread.currentThread().getContextClassLoader()).getName();
        } else if (type.getClassLoader() instanceof MultiParentClassLoader) {
            return ((MultiParentClassLoader) type.getClassLoader()).getName();
        } else {
            return Names.HOST_CONTRIBUTION;
        }
    }

    /**
     * Returns the contribution URI for the current context. In non-managed environments, {@link Names#HOST_CONTRIBUTION} will be returned.
     *
     * @return the contribution URI.
     */
    public static URI getContribution() {
        if (Thread.currentThread().getContextClassLoader() instanceof MultiParentClassLoader) {
            return ((MultiParentClassLoader) Thread.currentThread().getContextClassLoader()).getName();
        } else {
            return Names.HOST_CONTRIBUTION;
        }
    }

    private ContributionResolver() {
    }
}
