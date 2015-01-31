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
package org.fabric3.fabric.domain.generator.binding;

import java.util.List;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.domain.generator.binding.BindingMatchResult;

/**
 *
 */
public class NoSCABindingProviderException extends ContainerException {
    private static final long serialVersionUID = -7797860974206005955L;
    private transient List<BindingMatchResult> results;

    public NoSCABindingProviderException(String message, List<BindingMatchResult> results) {
        super(message);
        this.results = results;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());
        if (!results.isEmpty()) {
            builder.append("\nThe SCA binding selectors reported the following:\n");
            for (BindingMatchResult result : results) {
                builder.append(result.getType()).append("\n");
                for (String reason : result.getReasons()) {
                    builder.append("  ").append(reason).append("\n");
                }

            }
        }
        return builder.toString();
    }

}
