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
package org.fabric3.binding.file.generator;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.binding.file.model.FileBinding;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.binding.file.provision.FileBindingWireSource;
import org.fabric3.binding.file.provision.FileBindingWireTarget;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class FileWireBindingGeneratorTestCase extends TestCase {
    private FileWireBindingGenerator generator;

    public void testSourceGeneration() throws Exception {
        ServiceContract contract = createServiceContract();
        FileBinding definition = new FileBinding("binding", null, "location", Strategy.ARCHIVE, "archiveLocation", "error", null, null, 10);
        URI uri = URI.create("service");
        LogicalService service = new LogicalService(uri, null, null);
        LogicalBinding<FileBinding> logicalBinding = new LogicalBinding<>(definition, service);

        FileBindingWireSource source = generator.generateSource(logicalBinding, contract, Collections.<LogicalOperation>emptyList());
        assertNotNull(source.getLocation());
        assertNotNull(source.getArchiveLocation());
        assertEquals(Strategy.ARCHIVE, source.getStrategy());
    }

    public void testInvalidServiceContractGeneration() throws Exception {
        ServiceContract contract = new JavaServiceContract(Object.class); // invalid contract

        FileBinding definition = new FileBinding("binding", null, "location", Strategy.ARCHIVE, "archiveLocation", "error", null, null, 10);
        URI uri = URI.create("service");
        LogicalService service = new LogicalService(uri, null, null);
        LogicalBinding<FileBinding> logicalBinding = new LogicalBinding<>(definition, service);

        try {
            generator.generateSource(logicalBinding, contract, Collections.<LogicalOperation>emptyList());
            fail();
        } catch (Fabric3Exception e) {
            // expected
        }
    }

    public void testReferenceGeneration() throws Exception {
        ServiceContract contract = createReferenceContract();
        FileBinding definition = new FileBinding("binding", "location", "error");
        LogicalBinding<FileBinding> logicalBinding = new LogicalBinding<>(definition, null);

        FileBindingWireTarget target = generator.generateTarget(logicalBinding, contract, Collections.<LogicalOperation>emptyList());
        assertNotNull(target.getLocation());
    }

    private ServiceContract createServiceContract() {
        ServiceContract contract = new JavaServiceContract(FileTransport.class);
        DataType inputType = new JavaType(InputStream.class);
        List<DataType> input = Collections.<DataType>singletonList(inputType);
        List<DataType> faultType = Collections.emptyList();
        DataType outputType = new JavaType(Void.class);
        contract.setOperations(Collections.singletonList(new Operation("name", input, outputType, faultType)));
        return contract;
    }

    private ServiceContract createReferenceContract() {
        ServiceContract contract = new JavaServiceContract(FileReferenceTransport.class);
        DataType inputType = new JavaType(String.class);
        List<DataType> input = Collections.<DataType>singletonList(inputType);
        List<DataType> faultType = Collections.emptyList();
        DataType outputType = new JavaType(OutputStream.class);
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
        generator = new FileWireBindingGenerator(info);
    }
}
