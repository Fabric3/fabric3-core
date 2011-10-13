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
package org.fabric3.binding.file.runtime.sender;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev: 9763 $ $Date: 2011-01-03 01:48:06 +0100 (Mon, 03 Jan 2011) $
 */
public class FileSystemInterceptor implements Interceptor {
    private File outputDirectory;

    public FileSystemInterceptor(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public Message invoke(Message msg) {
        Object body = msg.getBody();
        if (body == null || !body.getClass().isArray()) {
            throw new ServiceRuntimeException("Invalid parameter type: " + body);
        }
        int length = Array.getLength(body);
        if (length != 1) {
            throw new ServiceRuntimeException("Invalid number of parameters: " + length);
        }
        Object element = Array.get(body, 0);
        if (element instanceof String) {
            throw new ServiceRuntimeException("Parameter must be a string: " + element);
        }
        File file = new File(outputDirectory, (String) element);
        try {
            OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
            msg.setBody(stream);
            return msg;
        } catch (FileNotFoundException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last one in an target interceptor chain");
    }

    public Interceptor getNext() {
        return null;
    }
}
