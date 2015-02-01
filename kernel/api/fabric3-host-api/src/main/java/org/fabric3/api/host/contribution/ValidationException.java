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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.host.failure.ValidationUtils;

/**
 * Base class for exceptions indicating a contribution has failed validation.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class ValidationException extends Fabric3Exception {
    private static final long serialVersionUID = -9097590343387033730L;

    private final List<ValidationFailure> errors;
    private final List<ValidationFailure> warnings;

    /**
     * Constructor that initializes the initial list of errors and warnings.
     *
     * @param errors   the list of errors
     * @param warnings the list of warnings
     */
    public ValidationException(List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        super("Validation errors were found");
        this.errors = errors;
        this.warnings = warnings;
    }

    /**
     * Constructor that initializes the initial list of errors and warnings.
     *
     * @param message  the message
     * @param errors   the list of errors
     * @param warnings the list of warnings
     */
    public ValidationException(String message, List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        super(message);
        this.errors = errors;
        this.warnings = warnings;
    }

    /**
     * Returns a collection of underlying errors associated with this exception.
     *
     * @return the collection of underlying errors
     */
    public List<ValidationFailure> getErrors() {
        return errors;
    }

    /**
     * Returns a collection of underlying warnings associated with this exception.
     *
     * @return the collection of underlying errors
     */
    public List<ValidationFailure> getWarnings() {
        return warnings;
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
