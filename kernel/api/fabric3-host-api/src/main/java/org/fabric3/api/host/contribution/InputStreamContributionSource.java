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
package org.fabric3.api.host.contribution;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.stream.InputStreamSource;
import org.fabric3.api.host.stream.Source;

/**
 * A contribution artifact that is sourced from an InputStream.
 */
public class InputStreamContributionSource implements ContributionSource {
    private URI uri;
    private Source source;

    public InputStreamContributionSource(URI uri, InputStream stream) {
        this.uri = uri;
        this.source = new InputStreamSource(uri.toString(), stream);
    }

    public URI getUri() {
        return uri;
    }

    public boolean persist() {
        return true;
    }

    public boolean isExtension() {
        return false;
    }

    public Source getSource() {
        return source;
    }

    public URL getLocation() {
        return null;
    }

    public long getTimestamp() {
        return 0;
    }

    public String getContentType() {
        return null;
    }
}