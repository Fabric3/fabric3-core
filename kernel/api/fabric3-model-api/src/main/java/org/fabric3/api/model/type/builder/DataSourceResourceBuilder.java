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
package org.fabric3.api.model.type.builder;

import java.util.ArrayList;

import org.fabric3.api.model.type.resource.datasource.DataSourceConfiguration;
import org.fabric3.api.model.type.resource.datasource.DataSourceResource;

/**
 * Builds {@link DataSourceResource}s.
 */
public class DataSourceResourceBuilder extends AbstractBuilder {
    private DataSourceResource definition;

    /**
     * Creates a builder.
     *
     * @return the builder
     */
    public static DataSourceResourceBuilder newBuilder() {
        return new DataSourceResourceBuilder();
    }

    private DataSourceResourceBuilder() {
        definition = new DataSourceResource(new ArrayList<DataSourceConfiguration>());
    }

    public DataSourceResourceBuilder add(DataSourceConfiguration configuration) {
        checkState();
        definition.getConfigurations().add(configuration);
        return this;
    }

    public DataSourceResource build() {
        checkState();
        freeze();
        return definition;
    }
}
