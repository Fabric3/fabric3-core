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
package org.fabric3.fabric.container.handler;

import org.fabric3.spi.container.channel.HandlerCreationException;

/**
 * Thrown when a transformer cannot be found.
 */
public class NoTransformerException extends HandlerCreationException {
    private static final long serialVersionUID = -3119130836236306468L;

    public NoTransformerException(String message) {
        super(message);
    }
}
