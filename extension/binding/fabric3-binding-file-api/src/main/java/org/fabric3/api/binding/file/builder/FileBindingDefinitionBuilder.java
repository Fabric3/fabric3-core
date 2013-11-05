/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
