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
package org.fabric3.api;

import org.oasisopen.sca.RequestContext;

/**
 * A Fabric3 extension to the OASIS SCA RequestContext API. Components may reference this interface when fields or setters marked with @Context instead of the
 * SCA RequestContext variant. For example:
 * <pre>
 * public class SomeComponent implements SomeService {
 *      &#064;Context
 *      protected F3RequestContext context;
 *      //...
 * }
 * </pre>
 * At runtime, the <code>context</code> field will be injected with an instance of F3RequestContext.
 */
public interface Fabric3RequestContext extends RequestContext {

    /**
     * Returns the Fabric3-specific security subject for the current request.
     *
     * @return the Fabric3-specific security subject for the current request
     */
    SecuritySubject getCurrentSubject();

    /**
     * Returns the header value corresponding to a name for the current request message.
     *
     * @param type the value type
     * @param name the header name
     * @return the header value or null if not found
     */
    <T> T getHeader(Class<T> type, String name);

    /**
     * Sets a header value for the current request context. Headers will be propagated across threads for non-blocking invocations made by a component when
     * processing a request. However, headers propagation across process boundaries is binding-specific. Some bindings may propagate headers while others may
     * ignore them.
     *
     * Note that header values should be immutable since, unlike purely synchronous programming models, SCA's asynchronous model may result in multiple threads
     * simultaneously accessing a header. For example, two non-blocking invocations to local services may access the same header.
     *
     * @param name  the header name
     * @param value the header value
     */
    void setHeader(String name, Object value);

    /**
     * Clears a header for the current request context.
     *
     * @param name the header name
     */
    void removeHeader(String name);

}