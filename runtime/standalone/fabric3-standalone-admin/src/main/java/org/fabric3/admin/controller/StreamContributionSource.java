/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.admin.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionSource;

/**
 * A ContributionSource that wraps an underlying input stream to avoid closing it. This implementation is used to handle input streams that contain
 * multiple contribution archives.
 *
 * @version $Rev$ $Date$
 */
public class StreamContributionSource implements ContributionSource {
    private URI uri;
    private StreamWrapper wrapped;

    public StreamContributionSource(URI uri, InputStream stream) {
        this.uri = uri;
        this.wrapped = new StreamWrapper(stream);
    }

    public boolean persist() {
        return true;
    }

    public URI getUri() {
        return uri;
    }

    public InputStream getSource() throws IOException {
        return wrapped;
    }

    public URL getLocation() {
        return null;
    }

    public long getTimestamp() {
        return 0;
    }

    public byte[] getChecksum() {
        return new byte[0];
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
        public void mark(int readlimit) {
            wrapped.mark(readlimit);
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