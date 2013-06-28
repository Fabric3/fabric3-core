/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.binding.file.runtime;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.fabric3.binding.file.api.InvalidDataException;
import org.fabric3.binding.file.api.ServiceAdapter;
import org.fabric3.host.util.IOHelper;

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
