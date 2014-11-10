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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.host.contribution;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Base exception class for contribution operations
 */
public class ContributionException extends Fabric3Exception {
    private static final long serialVersionUID = -719954756993485191L;

    public ContributionException(String message) {
        super(message);
    }

    public ContributionException(Throwable cause) {
        super(cause);
    }

    public ContributionException(String message, Throwable cause) {
        super(message, cause);
    }

}
