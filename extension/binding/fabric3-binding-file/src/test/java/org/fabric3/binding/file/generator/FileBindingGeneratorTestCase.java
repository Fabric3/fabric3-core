/*
 * Fabric3 Copyright (c) 2009-2013 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.file.generator;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.binding.file.common.Strategy;
import org.fabric3.binding.file.model.FileBindingDefinition;
import org.fabric3.binding.file.provision.FileBindingSourceDefinition;
import org.fabric3.binding.file.provision.FileBindingTargetDefinition;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 *
 */
public class FileBindingGeneratorTestCase extends TestCase {
    private FileBindingGenerator generator;

    private EffectivePolicy policy;

    public void testSourceGeneration() throws Exception {
        ServiceContract contract = createServiceContract();
        FileBindingDefinition definition =
                new FileBindingDefinition("binding", null, "location", Strategy.ARCHIVE, "archiveLocation", "error", null, null, 10);
        URI uri = URI.create("service");
        LogicalService service = new LogicalService(uri, null, null);
        LogicalBinding<FileBindingDefinition> logicalBinding = new LogicalBinding<FileBindingDefinition>(definition, service);

        FileBindingSourceDefinition physical = generator.generateSource(logicalBinding, contract, Collections.<LogicalOperation>emptyList(), policy);
        assertNotNull(physical.getLocation());
        assertNotNull(physical.getArchiveLocation());
        assertEquals(Strategy.ARCHIVE, physical.getStrategy());
    }

    public void testInvalidServiceContractGeneration() throws Exception {
        ServiceContract contract = new JavaServiceContract(Object.class); // invalid contract

        FileBindingDefinition definition =
                new FileBindingDefinition("binding", null, "location", Strategy.ARCHIVE, "archiveLocation", "error", null, null, 10);
        URI uri = URI.create("service");
        LogicalService service = new LogicalService(uri, null, null);
        LogicalBinding<FileBindingDefinition> logicalBinding = new LogicalBinding<FileBindingDefinition>(definition, service);

        try {
            generator.generateSource(logicalBinding, contract, Collections.<LogicalOperation>emptyList(), policy);
            fail();
        } catch (InvalidContractException e) {
            // expected
        }
    }

    public void testReferenceGeneration() throws Exception {
        ServiceContract contract = createReferenceContract();
        FileBindingDefinition definition = new FileBindingDefinition("binding", "location", "error");
        LogicalBinding<FileBindingDefinition> logicalBinding = new LogicalBinding<FileBindingDefinition>(definition, null);

        FileBindingTargetDefinition physical = generator.generateTarget(logicalBinding, contract, Collections.<LogicalOperation>emptyList(), policy);
        assertNotNull(physical.getLocation());
    }

    private ServiceContract createServiceContract() {
        ServiceContract contract = new JavaServiceContract(FileTransport.class);
        DataType inputType = new JavaClass<InputStream>(InputStream.class);
        List<DataType<?>> input = Collections.<DataType<?>>singletonList(inputType);
        List<DataType<?>> faultType = Collections.emptyList();
        DataType<?> outputType = new JavaClass<Void>(Void.class);
        contract.setOperations(Collections.singletonList(new Operation("name", input, outputType, faultType)));
        return contract;
    }

    private ServiceContract createReferenceContract() {
        ServiceContract contract = new JavaServiceContract(FileReferenceTransport.class);
        DataType inputType = new JavaClass<String>(String.class);
        List<DataType<?>> input = Collections.<DataType<?>>singletonList(inputType);
        List<DataType<?>> faultType = Collections.emptyList();
        DataType<?> outputType = new JavaClass<OutputStream>(OutputStream.class);
        contract.setOperations(Collections.singletonList(new Operation("name", input, outputType, faultType)));
        return contract;
    }

    private static interface FileTransport {
        void receive(InputStream stream);
    }

    private static interface FileReferenceTransport {
        OutputStream onStream(String id);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        generator = new FileBindingGenerator(info);
        policy = EasyMock.createNiceMock(EffectivePolicy.class);
    }
}
