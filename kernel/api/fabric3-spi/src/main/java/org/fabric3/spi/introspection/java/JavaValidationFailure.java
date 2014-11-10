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
package org.fabric3.spi.introspection.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.fabric3.api.host.failure.ValidationFailure;
import org.fabric3.api.model.type.ModelObject;

/**
 * Base class for validation failures occurring in Java artifacts.
 */
public abstract class JavaValidationFailure extends ValidationFailure {
    private List<Object> sources;
    private transient Object codeLocation;

    public JavaValidationFailure(Object codeLocation, ModelObject... sources) {
        this.sources = new ArrayList<>();
        if (sources != null) {
            this.sources.addAll(Arrays.asList(sources));
        }
        this.codeLocation = codeLocation;
    }

    public List<Object> getSources() {
        return sources;
    }

    public Object getCodeLocation() {
        return codeLocation;
    }

}
