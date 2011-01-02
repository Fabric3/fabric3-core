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
package org.fabric3.binding.ftp.runtime;

import java.io.InputStream;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Wire;
import org.fabric3.transport.ftp.api.FtpConstants;
import org.fabric3.transport.ftp.api.FtpLet;

/**
 * Handles incoming FTP puts from the protocol stack.
 *
 * @version $Rev$ $Date$
 */
public class BindingFtpLet implements FtpLet {
    private String servicePath;
    private Wire wire;
    private Interceptor interceptor;
    private BindingMonitor monitor;

    public BindingFtpLet(String servicePath, Wire wire, BindingMonitor monitor) {
        this.servicePath = servicePath;
        this.wire = wire;
        this.monitor = monitor;
    }

    public boolean onUpload(String fileName, String contentType, InputStream uploadData) throws Exception {
        Object[] args = new Object[]{fileName, uploadData};
        WorkContext workContext = new WorkContext();
        // set the header value for the request context
        workContext.setHeader(FtpConstants.HEADER_CONTENT_TYPE, contentType);
        Message input = new MessageImpl(args, false, workContext);
        Message msg = getInterceptor().invoke(input);
        if (msg.isFault()) {
            monitor.fileProcessingError(servicePath, (Throwable) msg.getBody());
            return false;
        }
        return true;
    }

    private Interceptor getInterceptor() {
        // lazy load the interceptor as it may not have been added when the instance was created in the wire attacher
        if (interceptor == null) {
            interceptor = wire.getInvocationChains().iterator().next().getHeadInterceptor();
        }
        return interceptor;
    }
}
