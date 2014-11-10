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
package org.fabric3.binding.ws.metro.runtime.core;

import java.net.MalformedURLException;
import java.net.URL;

import com.sun.xml.ws.api.ResourceLoader;
import com.sun.xml.ws.api.server.Container;

/**
 * Wraps a Metro Container with one that resolves dynamically generated WSDL containing policy configuration for an endpoint.
 */
public class WsitConfigurationContainer extends Container {
    private Container delegate;
    private URL wsitConfiguration;

    public WsitConfigurationContainer(Container delegate, URL wsitConfiguration) {
        this.delegate = delegate;
        this.wsitConfiguration = wsitConfiguration;
    }


    private final ResourceLoader loader = new ResourceLoader() {
        public URL getResource(String resource) throws MalformedURLException {
            if (resource.startsWith("wsit-")) {
                return wsitConfiguration;
            }
            return delegate.getSPI(ResourceLoader.class).getResource(resource);
        }
    };

    public <T> T getSPI(Class<T> spiType) {
        if (spiType == ResourceLoader.class) {
            return spiType.cast(loader);
        }
        return delegate.getSPI(spiType);
    }


}
