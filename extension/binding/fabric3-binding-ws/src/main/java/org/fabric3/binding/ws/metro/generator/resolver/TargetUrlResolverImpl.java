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
package org.fabric3.binding.ws.metro.generator.resolver;

import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.api.binding.ws.model.WsBinding;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of TargetUrlResolver.
 */
public class TargetUrlResolverImpl implements TargetUrlResolver {
    private ServletHost servletHost;
    private HostInfo hostInfo;

    /**
     * Constructor.
     *
     * @param servletHost the servlet host, used for determining the endpoint port  in a single-VM environment
     * @param hostInfo    the host info
     */
    public TargetUrlResolverImpl(@Reference ServletHost servletHost, @Reference HostInfo hostInfo) {
        this.servletHost = servletHost;
        this.hostInfo = hostInfo;
    }

    public URL resolveUrl(LogicalBinding<WsBinding> binding) throws GenerationException {
        try {
            URL targetUrl;
            String path = binding.getDefinition().getTargetUri().toString();
            if (path == null) {
                path = binding.getParent().getUri().getFragment();
            }
            boolean https = false;
            if (RuntimeMode.VM == hostInfo.getRuntimeMode()) {
                // single VM
                if (https) {
                    targetUrl = new URL("https://localhost:" + servletHost.getHttpsPort() + "/" + path);
                } else {
                    targetUrl = new URL("http://localhost:" + servletHost.getHttpPort() + "/" + path);
                }

            } else {
                throw new GenerationException("Resolve URL not supported in distributed configuration");
            }
            return targetUrl;
        } catch (MalformedURLException e) {
            throw new GenerationException(e);
        }

    }

}
