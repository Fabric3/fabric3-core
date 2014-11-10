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
package org.fabric3.binding.web.runtime.channel;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.web.runtime.common.InvalidContentTypeException;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.spi.model.type.json.JsonType;

import static org.fabric3.binding.web.runtime.common.ContentTypes.APPLICATION_JSON;
import static org.fabric3.binding.web.runtime.common.ContentTypes.TEXT_PLAIN;

/**
 *
 */
public final class ChannelUtils {
    private static final JsonType JSON = new JsonType(String.class);

    public static EventWrapper createWrapper(String contentType, String data) throws InvalidContentTypeException {
        DataType eventType;
        if ((contentType == null)) {
            throw new ServiceRuntimeException("No content type specified: " + contentType);
        } else if (contentType.contains(APPLICATION_JSON) || contentType.contains(TEXT_PLAIN)) {
            eventType = JSON;
        } else {
            throw new InvalidContentTypeException("Unsupported content type: " + contentType);
        }
        return new EventWrapper(eventType, data);
    }

}
