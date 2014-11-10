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
package org.fabric3.spi.introspection.validation;

import java.util.Comparator;

import org.fabric3.api.host.contribution.ArtifactValidationFailure;
import org.fabric3.api.host.failure.ValidationFailure;

/**
 * Orders ValidationFailures. ArtifactValidationFailures are ordered after other types.
 */
public class ValidationExceptionComparator implements Comparator<ValidationFailure> {
    public int compare(ValidationFailure first, ValidationFailure second) {
        if (first instanceof ArtifactValidationFailure && !(second instanceof ArtifactValidationFailure)) {
            return -1;
        } else if (!(first instanceof ArtifactValidationFailure) && second instanceof ArtifactValidationFailure) {
            return 1;
        } else {
            return 0;
        }
    }
}
