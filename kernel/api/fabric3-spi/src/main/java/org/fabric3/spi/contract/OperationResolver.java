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
package org.fabric3.spi.contract;

import java.util.List;

import org.fabric3.spi.model.instance.LogicalOperation;

/**
 * Resolves a source operation against a collection of target operations when the interface contracts are different but compatible, i.e. Java and
 * WSDL.
 */
public interface OperationResolver {

    /**
     * Resolves the source operation against a set of targets.
     *
     * @param source  the source operation to resolve
     * @param targets the target operations to resolve against
     * @return the resolved operation
     * @throws OperationNotFoundException if a target operation cannot be found
     */
    LogicalOperation resolve(LogicalOperation source, List<LogicalOperation> targets) throws OperationNotFoundException;

}
