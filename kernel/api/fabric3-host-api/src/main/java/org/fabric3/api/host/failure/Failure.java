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
package org.fabric3.api.host.failure;

import java.util.Collections;
import java.util.List;

/**
 * The base type for all failures raised in the system. Failures are raised during contribution introspection in response to a syntactic error or
 * during deployment when an logical instantiation or wiring exception is encountered.
 */
public abstract class Failure {

    /**
     * Returns the message associated with the error.
     *
     * @return the message associated with the error.
     */
    public abstract String getMessage();

    /**
     * Returns the abbreviated  message associated with the error.
     *
     * @return the abbreviated message associated with the error.
     */
    public String getShortMessage() {
        return getMessage();
    }

    /**
     * Returns the objects that are a source of a failure.
     *
     * @return the failure sources
     */
    public List<?> getSources() {
        return Collections.emptyList();
    }


}
