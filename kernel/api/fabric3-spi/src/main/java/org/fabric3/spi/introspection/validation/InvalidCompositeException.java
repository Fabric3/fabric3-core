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
package org.fabric3.spi.introspection.validation;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

import org.fabric3.api.host.contribution.ValidationException;
import org.fabric3.api.host.failure.ValidationFailure;

/**
 *
 */
public class InvalidCompositeException extends ValidationException {
    private static final long serialVersionUID = -2678786389599538999L;

    private final QName name;

    /**
     * Constructor.
     *
     * @param name     the qualified name of the composite that failed validation
     * @param errors   the errors that were found during validation
     * @param warnings the warnings that were found during validation
     */
    public InvalidCompositeException(QName name, List<ValidationFailure> errors, List<ValidationFailure> warnings) {
        super(errors, warnings);
        this.name = name;
    }

    public QName getCompositeName() {
        return name;
    }

    public String getMessage() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);
        ValidationUtils.writeErrors(writer, getErrors());
        writer.write("\n");
        ValidationUtils.writeWarnings(writer, getWarnings());
        return bas.toString();
    }

}
