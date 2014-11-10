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
package org.fabric3.binding.file.runtime;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.fabric3.api.binding.file.InvalidDataException;
import org.fabric3.api.binding.file.ServiceAdapter;
import org.fabric3.api.host.util.IOHelper;

/**
 * A {@link ServiceAdapter} implementation that passes a DataHandler to the target service.
 */
@SuppressWarnings({"ResultOfMethodCallIgnored"})
public class DataHandlerServiceAdapter extends AbstractFileServiceAdapter {

    public Object[] beforeInvoke(File file) throws InvalidDataException {
        try {
            DataHandler dataHandler = new DataHandler(new CloseableDataSource(file));
            return new Object[]{dataHandler};
        } catch (IOException e) {
            throw new InvalidDataException(e);
        }
    }

    public void afterInvoke(File file, Object[] payload) {
        if (payload.length != 1) {
            throw new AssertionError("Invalid payload length: " + payload.length);
        }
        if (!(payload[0] instanceof DataHandler)) {
            throw new AssertionError("Invalid payload type: " + payload[0]);
        }
        DataHandler dataHandler = (DataHandler) payload[0];
        try {
            IOHelper.closeQuietly(dataHandler.getInputStream());
        } catch (IOException e) {
            // ignore as this will not happen
        }
    }

    private class CloseableDataSource implements DataSource {
        private InputStream inputStream;

        private CloseableDataSource(File file) throws FileNotFoundException {
            this.inputStream = new FileInputStream(file);
        }

        public InputStream getInputStream() {
            return inputStream;
        }

        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getContentType() {
            throw new UnsupportedOperationException();
        }

        public String getName() {
            return getClass().getName();
        }
    }

}
