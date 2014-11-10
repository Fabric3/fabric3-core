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
package org.fabric3.api.host.domain;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fabric3.api.host.failure.AssemblyFailure;

/**
 * Denotes a recoverable failure updating the domain assembly during deployment. For example, a failure may be a reference targeted to a non-existent
 * service.
 */
public class AssemblyException extends DeploymentException {
    private static final long serialVersionUID = 3957908169593535300L;
    private static final Comparator<AssemblyFailure> COMPARATOR = new Comparator<AssemblyFailure>() {
        public int compare(AssemblyFailure first, AssemblyFailure second) {
            return first.getComponentUri().compareTo(second.getComponentUri());
        }
    };

    private transient final List<AssemblyFailure> errors;

    public AssemblyException(List<AssemblyFailure> errors) {
        this.errors = errors;
    }

    public List<AssemblyFailure> getErrors() {
        return errors;
    }

    public String getMessage() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);

        if (!errors.isEmpty()) {
            List<AssemblyFailure> sorted = new ArrayList<>(errors);
            // sort the errors by component
            Collections.sort(sorted, COMPARATOR);
            for (AssemblyFailure error : sorted) {
                writer.write(error.getMessage() + " (" + error.getContributionUri() + ")");
                writer.write("\n\n");
            }
        }
        writer.flush();
        return bas.toString();

    }
}
