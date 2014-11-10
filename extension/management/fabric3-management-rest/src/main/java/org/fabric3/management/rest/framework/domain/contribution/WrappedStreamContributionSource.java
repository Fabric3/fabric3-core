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
package org.fabric3.management.rest.framework.domain.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.stream.InputStreamSource;
import org.fabric3.api.host.stream.Source;

/**
 * A ContributionSource that wraps an underlying input stream to avoid closing it. This implementation is used to handle streams that contain multiple
 * contribution archives.
 */
public class WrappedStreamContributionSource implements ContributionSource {
    private URI uri;
    private InputStreamSource source;
    private boolean extension;

    public WrappedStreamContributionSource(URI uri, InputStream stream, boolean extension) {
        this.uri = uri;
        this.extension = extension;
        StreamWrapper wrapper = new StreamWrapper(stream);
        this.source = new InputStreamSource(uri.toString(), wrapper);

    }

    public URI getUri() {
        return uri;
    }

    public boolean persist() {
        return true;
    }

    public boolean isExtension() {
        return extension;
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

    private class StreamWrapper extends InputStream {
        private InputStream wrapped;

        private StreamWrapper(InputStream wrapped) {
            this.wrapped = wrapped;
        }

        public int read() throws IOException {
            return wrapped.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return wrapped.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return wrapped.read(b, off, len);
        }

        @Override
        public long skip(long n) throws IOException {
            return wrapped.skip(n);
        }

        @Override
        public int available() throws IOException {
            return wrapped.available();
        }

        @Override
        public void close() throws IOException {
            // do nothing
        }

        @Override
        public void mark(int readLimit) {
            wrapped.mark(readLimit);
        }

        @Override
        public void reset() throws IOException {
            wrapped.reset();
        }

        @Override
        public boolean markSupported() {
            return wrapped.markSupported();
        }

        @Override
        public int hashCode() {
            return wrapped.hashCode();
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }

    }
}