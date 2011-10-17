/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.fabric3.binding.file.api.FileBindingAdapter;
import org.fabric3.binding.file.api.InvalidDataException;
import org.fabric3.host.util.IOHelper;

/**
 * The default {@link FileBindingAdapter} implementation. Opens a buffered stream for reading the specified file.
 *
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class DefaultFileBindingAdapter implements FileBindingAdapter {


    public Object[] beforeInvoke(File file) throws InvalidDataException {
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(file);
            return new Object[]{new BufferedInputStream(fileStream)};
        } catch (FileNotFoundException e) {
            IOHelper.closeQuietly(fileStream);
            throw new InvalidDataException(e);
        }
    }

    public void afterInvoke(File file, Object[] payload) {
        if (payload.length != 1) {
            throw new AssertionError("Invalid payload length: " + payload.length);
        }
        if (!(payload[0] instanceof Closeable)) {
            throw new AssertionError("Invalid payload type: " + payload[0]);
        }
        IOHelper.closeQuietly((Closeable) payload[0]);
    }
}
