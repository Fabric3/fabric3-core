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
package org.fabric3.api.host.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * A Source that wraps an InputStream.
 */
public class InputStreamSource implements Source {
    private String systemId;
    private InputStream source;
    private boolean opened = false;

    public InputStreamSource(String systemId, InputStream source) {
        this.systemId = systemId;
        this.source = source;
    }

    public String getSystemId() {
        return systemId;
    }

    public URL getBaseLocation() {
        return null;
    }

    public InputStream openStream() throws IOException {
        if (opened) {
            throw new IllegalStateException("Input stream can only be opened once");
        }
        opened = true;
        return source;
    }

}
