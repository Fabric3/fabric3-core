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
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.XMLStreamReader;

/**
 * Denotes an invalid QName prefix.
 */
public class InvalidPrefixException extends LoaderException {
    private static final long serialVersionUID = -4896928793798546890L;
    private String prefix;

    public InvalidPrefixException(String message, String prefix, XMLStreamReader reader) {
        super(message, reader);
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }
}
