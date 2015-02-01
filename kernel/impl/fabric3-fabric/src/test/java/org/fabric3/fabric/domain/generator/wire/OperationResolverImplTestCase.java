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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class OperationResolverImplTestCase extends TestCase {

    public void testResolveOperation() throws Exception {
        OperationResolverImpl resolver = new OperationResolverImpl();

        LogicalOperation operation1 = createOperation("op", String.class);
        LogicalOperation operation1b = createOperation("op", String.class);
        LogicalOperation operation2 = createOperation("op", Integer.class);

        List<LogicalOperation> targets = new ArrayList<>();
        targets.add(operation1b);
        targets.add(operation2);

        assertSame(operation1b, resolver.resolve(operation1, targets));
    }

    public void testUnResolvableOperation() throws Exception {
        OperationResolverImpl resolver = new OperationResolverImpl();

        LogicalOperation operation1 = createOperation("op", String.class);
        LogicalOperation operation2 = createOperation("op", Integer.class);

        List<LogicalOperation> targets = new ArrayList<>();
        targets.add(operation2);

        try {
            resolver.resolve(operation1, targets);
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }
    }

    private <T> LogicalOperation createOperation(String name, Class<T> inputType) {
        List<DataType> input = new ArrayList<>();
        JavaType type = new JavaType(inputType);
        input.add(type);
        DataType output = new JavaType(String.class);
        List<DataType> faults = new ArrayList<>();
        faults.add(new JavaType(Exception.class));
        Operation definition = new Operation(name, input, output, faults);
        return new LogicalOperation(definition, null);
    }

}