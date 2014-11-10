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
package org.fabric3.api.binding.file.builder;

import org.fabric3.api.model.type.builder.AbstractBuilder;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.binding.file.model.FileBindingDefinition;

/**
 * Builder for the File binding.
 */
public class FileBindingDefinitionBuilder extends AbstractBuilder {
    private FileBindingDefinition binding;

    public static FileBindingDefinitionBuilder newBuilder() {
        return new FileBindingDefinitionBuilder();
    }

    public FileBindingDefinitionBuilder() {
        this("file.binding");
    }

    public FileBindingDefinitionBuilder(String name) {
        this.binding = new FileBindingDefinition(name);
    }

    public FileBindingDefinitionBuilder adapter(String uri) {
        checkState();
        binding.setAdapterUri(uri);
        return this;
    }

    public FileBindingDefinitionBuilder archiveLocation(String location) {
        checkState();
        binding.setArchiveLocation(location);
        return this;
    }

    public FileBindingDefinitionBuilder delay(long delay) {
        checkState();
        binding.setDelay(delay);
        return this;
    }

    public FileBindingDefinitionBuilder errorLocation(String location) {
        checkState();
        binding.setErrorLocation(location);
        return this;
    }

    public FileBindingDefinitionBuilder location(String location) {
        checkState();
        binding.setLocation(location);
        return this;
    }

    public FileBindingDefinitionBuilder pattern(String pattern) {
        checkState();
        binding.setPattern(pattern);
        return this;
    }

    public FileBindingDefinitionBuilder strategy(Strategy strategy) {
        checkState();
        binding.setStrategy(strategy);
        return this;
    }

    public FileBindingDefinition build() {
        checkState();
        freeze();
        return binding;
    }

}
