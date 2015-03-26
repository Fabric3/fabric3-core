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
package org.fabric3.datasource.generator;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.datasource.model.DataSourceResourceReference;
import org.fabric3.datasource.provision.DataSourceWireTarget;
import org.fabric3.spi.domain.generator.resource.ResourceReferenceGenerator;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class DataSourceResourceReferenceGenerator implements ResourceReferenceGenerator<DataSourceResourceReference> {

    public DataSourceWireTarget generateWireTarget(LogicalResourceReference<DataSourceResourceReference> reference) throws Fabric3Exception {
        DataSourceResourceReference definition = reference.getDefinition();
        String dataSourceName = definition.getDataSourceName();
        boolean optional = definition.isOptional();
        return new DataSourceWireTarget(dataSourceName, optional);
    }
}