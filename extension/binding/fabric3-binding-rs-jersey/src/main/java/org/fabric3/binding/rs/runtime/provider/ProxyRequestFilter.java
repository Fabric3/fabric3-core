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
package org.fabric3.binding.rs.runtime.provider;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Dispatches to a component-based filter.
 * <p/>
 * This implementation performs a lazy lookup of the component instance since filters are provisioned with composite resources, which occurs before components
 * are provisioned.
 */
@Provider
public class ProxyRequestFilter extends AbstractProxyProvider<ContainerRequestFilter> implements ContainerRequestFilter {

    public void filter(ContainerRequestContext requestContext) throws IOException {
        getDelegate().filter(requestContext);
    }
}
