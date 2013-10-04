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
package org.fabric3.fabric.deployment.generator.wire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.spi.contract.OperationNotFoundException;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.java.JavaType;
import org.fabric3.spi.model.type.xsd.XSDSimpleType;
import org.fabric3.spi.model.type.xsd.XSDType;

/**
 *
 */
public class OperationResolverImplTestCase extends TestCase {
    private static final QName STRING_QNAME = new QName(XSDType.XSD_NS, "string");


    public void testResolveOperation() throws Exception {
        OperationResolverImpl resolver = new OperationResolverImpl();

        LogicalOperation operation1 = createOperation("op", String.class);
        LogicalOperation operation1b = createOperation("op", String.class);
        LogicalOperation operation2 = createOperation("op", Integer.class);

        List<LogicalOperation> targets = new ArrayList<LogicalOperation>();
        targets.add(operation1b);
        targets.add(operation2);

        assertSame(operation1b, resolver.resolve(operation1, targets));
    }

    public void testUnResolvableOperation() throws Exception {
        OperationResolverImpl resolver = new OperationResolverImpl();

        LogicalOperation operation1 = createOperation("op", String.class);
        LogicalOperation operation2 = createOperation("op", Integer.class);

        List<LogicalOperation> targets = new ArrayList<LogicalOperation>();
        targets.add(operation2);

        try {
            resolver.resolve(operation1, targets);
            fail();
        } catch (OperationNotFoundException e) {
            // expected
        }
    }

    public void testResolveJavaToXsdOperation() throws Exception {
        OperationResolverImpl resolver = new OperationResolverImpl();

        LogicalOperation operation1 = createOperation("op", String.class);
        ((JavaType) operation1.getDefinition().getInputTypes().get(0)).setXsdType(STRING_QNAME);
        LogicalOperation operation1b = createXsdOperation("op");

        List<LogicalOperation> targets = new ArrayList<LogicalOperation>();
        targets.add(operation1b);

        assertSame(operation1b, resolver.resolve(operation1, targets));
    }

    private <T> LogicalOperation createOperation(String name, Class<T> inputType) {
        List<DataType<?>> input = new ArrayList<DataType<?>>();
        JavaClass<T> type = new JavaClass<T>(inputType);
        input.add(type);
        DataType<?> output = new JavaClass<String>(String.class);
        List<DataType<?>> faults = new ArrayList<DataType<?>>();
        faults.add(new JavaClass<Exception>(Exception.class));
        Operation definition = new Operation(name, input, output, faults);
        return new LogicalOperation(definition, null);
    }

    private <T> LogicalOperation createXsdOperation(String name) {
        DataType<?> stringType = new XSDSimpleType(String.class, STRING_QNAME);
        List<DataType<?>> input = new ArrayList<DataType<?>>();
        input.add(stringType);
        Operation definition = new Operation(name, input, stringType, Collections.<DataType<?>>emptyList());
        return new LogicalOperation(definition, null);
    }
}