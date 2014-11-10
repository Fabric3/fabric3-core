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
package org.fabric3.container.web.spi;

import javax.servlet.http.HttpServletRequest;

/**
 * Associates the current web request with the thread processing it so that contexts such as the HTTP session can be accessed by the web component
 * container .
 */
public final class WebRequestTunnel {
    private static final ThreadLocal<HttpServletRequest> REQUEST = new ThreadLocal<>();

    private WebRequestTunnel() {
    }

    public static void setRequest(HttpServletRequest request) {
        REQUEST.set(request);
    }

    public static HttpServletRequest getRequest() {
        return REQUEST.get();
    }

}
