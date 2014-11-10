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
package org.fabric3.binding.file.runtime.sender;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.api.binding.file.ReferenceAdapter;
import org.fabric3.spi.container.invocation.Message;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * Attached to bound reference invocation chains. Provides a non-transactional output stream for writing a file to the configured location.
 */
public class FileSystemInterceptor implements Interceptor {
    private File outputDirectory;
    private ReferenceAdapter adapter;

    public FileSystemInterceptor(File outputDirectory, ReferenceAdapter adapter) {
        this.outputDirectory = outputDirectory;
        this.adapter = adapter;
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
        if (!(element instanceof String)) {
            throw new ServiceRuntimeException("Parameter must be a string: " + element);
        }
        File file = new File(outputDirectory, (String) element);
        try {
            OutputStream stream = adapter.createOutputStream(file);
            msg.setBody(stream);
            return msg;
        } catch (IOException e) {
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
