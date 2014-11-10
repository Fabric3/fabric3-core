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
package org.fabric3.node.domain;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.spi.introspection.validation.ValidationUtils;

/**
 *
 */
public class ValidationDeploymentException extends DeploymentException {
    private static final long serialVersionUID = 1568105611963957413L;

    private transient List<ValidationFailure> errors;
    private transient List<ValidationFailure> warnings;

    public ValidationDeploymentException(List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        this.errors = errors;
        this.warnings = warnings;
    }

    public String getMessage() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);
        ValidationUtils.writeErrors(writer, errors);
        writer.write("\n");
        ValidationUtils.writeWarnings(writer, warnings);
        return bas.toString();
    }
}
