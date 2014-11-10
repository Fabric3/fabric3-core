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
package org.fabric3.runtime.weblogic.cluster;

/**
 * Used to report exceptions when communicating through a {@link RuntimeChannel}.
 * <p/>
 * Note nested exception classes must be available on the WebLogic server classpath as instances are passed through the WebLogic RMI layer.
 */
public class ChannelException extends Exception {
    private static final long serialVersionUID = 6699558378799219503L;

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelException(Throwable e) {
        super(e);
    }

    public ChannelException(String message) {
        super(message);
    }
}
