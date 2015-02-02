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
package org.fabric3.fabric.domain.generator.wire;

import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.contract.OperationResolver;
import org.fabric3.spi.model.instance.LogicalInvocable;
import org.fabric3.spi.model.instance.LogicalOperation;

/**
 *
 */
public class OperationResolverImpl implements OperationResolver {

    public LogicalOperation resolve(LogicalOperation source, List<LogicalOperation> targets) {
        Operation sourceDefinition = source.getDefinition();
        for (LogicalOperation target : targets) {
            Operation targetDefinition = target.getDefinition();

            if (sourceDefinition.getName().equals(targetDefinition.getName())) {
                List<DataType> sourceInputTypes = sourceDefinition.getInputTypes();
                DataType sourceOutputType = sourceDefinition.getOutputType();
                DataType targetOutputType = targetDefinition.getOutputType();
                if (sourceOutputType.equals(targetOutputType) && sourceInputTypes.equals(targetDefinition.getInputTypes())) {
                    return target;
                }
            }
        }
        LogicalInvocable parent = source.getParent();
        if (parent != null) {
            String sourceComponent = parent.getParent().getUri().toString();
            throw new Fabric3Exception("Target operation not found for " + sourceDefinition.getName() + " on source component " + sourceComponent);
        } else {
            throw new Fabric3Exception("Target operation not found for " + sourceDefinition.getName());
        }
    }

}