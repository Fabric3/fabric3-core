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

import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.binding.file.model.FileBinding;
import org.fabric3.api.model.type.builder.AbstractBuilder;

/**
 * Builder for the File binding.
 */
public class FileBindingBuilder extends AbstractBuilder {
    private FileBinding binding;

    public static FileBindingBuilder newBuilder() {
        return new FileBindingBuilder();
    }

    public FileBindingBuilder() {
        this("file.binding");
    }

    public FileBindingBuilder(String name) {
        this.binding = new FileBinding(name);
    }

    public FileBindingBuilder adapter(String uri) {
        checkState();
        binding.setAdapterUri(uri);
        return this;
    }

    public FileBindingBuilder archiveLocation(String location) {
        checkState();
        binding.setArchiveLocation(location);
        return this;
    }

    public FileBindingBuilder delay(long delay) {
        checkState();
        binding.setDelay(delay);
        return this;
    }

    public FileBindingBuilder errorLocation(String location) {
        checkState();
        binding.setErrorLocation(location);
        return this;
    }

    public FileBindingBuilder location(String location) {
        checkState();
        binding.setLocation(location);
        return this;
    }

    public FileBindingBuilder pattern(String pattern) {
        checkState();
        binding.setPattern(pattern);
        return this;
    }

    public FileBindingBuilder strategy(Strategy strategy) {
        checkState();
        binding.setStrategy(strategy);
        return this;
    }

    public FileBinding build() {
        checkState();
        freeze();
        return binding;
    }

}
