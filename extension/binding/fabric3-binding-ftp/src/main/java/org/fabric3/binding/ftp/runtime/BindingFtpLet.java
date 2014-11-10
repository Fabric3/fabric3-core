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
package org.fabric3.binding.ftp.runtime;

import java.io.InputStream;

import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.invocation.MessageCache;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.wire.Interceptor;
import org.fabric3.spi.container.wire.Wire;
import org.fabric3.transport.ftp.api.FtpConstants;
import org.fabric3.transport.ftp.api.FtpLet;

/**
 * Handles incoming FTP puts from the protocol stack.
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
        WorkContext workContext = WorkContextCache.getAndResetThreadWorkContext();
        Message input = MessageCache.getAndResetMessage();
        try {
            // set the header value for the request context
            workContext.setHeader(FtpConstants.HEADER_CONTENT_TYPE, contentType);
            input.setWorkContext(workContext);
            input.setBody(args);
            Message response = getInterceptor().invoke(input);
            if (response.isFault()) {
                monitor.fileProcessingError(servicePath, (Throwable) response.getBody());
                input.reset();
                return false;
            }
            return true;
        } finally {
            input.reset();
            workContext.reset();

        }
    }

    private Interceptor getInterceptor() {
        // lazy load the interceptor as it may not have been added when the instance was created in the wire attacher
        if (interceptor == null) {
            interceptor = wire.getInvocationChains().iterator().next().getHeadInterceptor();
        }
        return interceptor;
    }
}
