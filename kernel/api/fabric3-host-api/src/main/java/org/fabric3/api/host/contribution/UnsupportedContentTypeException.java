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
package org.fabric3.api.host.contribution;

/**
 * Exception thrown to indicate that a Content-Type is not supported by this SCA Domain. The Content-Type value supplied will be returned as the
 * message text for this exception.
 */
public class UnsupportedContentTypeException extends InstallException {
    private static final long serialVersionUID = -1831797280021355672L;

    /**
     * Constructor specifying the Content-Type value that is not supported and an identifier to use with this exception (typically the resource being
     * processed).
     *
     * @param message    the error message
     */
    public UnsupportedContentTypeException(String message) {
        super(message);
    }
}
