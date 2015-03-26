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

import javax.activation.DataHandler;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.binding.file.model.FileBinding;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.binding.file.provision.FileBindingWireSource;
import org.fabric3.binding.file.provision.FileBindingWireTarget;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.fabric3.spi.model.type.java.JavaType;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
@Key("org.fabric3.api.binding.file.model.FileBinding")
public class FileWireBindingGenerator implements WireBindingGenerator<FileBinding> {
    private static final String REGEX_ALL = ".*";
    private HostInfo info;

    private long defaultDelay = 2000;

    @Property(required = false)
    public void setDelay(long delay) {
        this.defaultDelay = delay;
    }

    public FileWireBindingGenerator(@Reference HostInfo info) {
        this.info = info;
    }

    public FileBindingWireSource generateSource(LogicalBinding<FileBinding> binding, ServiceContract contract, List<LogicalOperation> operations)
            throws Fabric3Exception {
        validateServiceContract(contract);
        boolean dataHandler = isDataHandler(contract);
        FileBinding definition = binding.getDefinition();
        String pattern = definition.getPattern();
        if (pattern == null) {
            pattern = REGEX_ALL;
        }
        String location = definition.getLocation();
        Strategy strategy = definition.getStrategy();
        String archiveLocation = definition.getArchiveLocation();
        URI uri = binding.getParent().getUri();
        String errorLocation = definition.getErrorLocation();
        if (errorLocation == null) {
            throw new Fabric3Exception("Error location must be specified on the file binding configuration for " + uri);
        }
        String adapterClass = definition.getAdapterClass();
        URI adaptorUri = getAdaptorUri(definition);

        long delay = definition.getDelay();
        if (delay == -1) {
            delay = defaultDelay;
        }
        return new FileBindingWireSource(uri,
                                                   pattern,
                                                   location,
                                                   strategy,
                                                   archiveLocation,
                                                   errorLocation,
                                                   adapterClass,
                                                   adaptorUri,
                                                   delay,
                                                   dataHandler);
    }

    public FileBindingWireTarget generateTarget(LogicalBinding<FileBinding> binding, ServiceContract contract, List<LogicalOperation> operations)
            throws Fabric3Exception {
        validateReferenceContract(contract);
        FileBinding definition = binding.getDefinition();
        String location = definition.getLocation();
        String adapterClass = definition.getAdapterClass();
        URI adaptorUri = getAdaptorUri(definition);
        return new FileBindingWireTarget(location, adapterClass, adaptorUri);
    }

    public PhysicalWireTarget generateServiceBindingTarget(LogicalBinding<FileBinding> binding,
                                                                     ServiceContract contract,
                                                                     List<LogicalOperation> operations) throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Validates a service contract for a bound reference. The service contract must contain exactly one operation of the form:
     * <pre>
     * OutputStream openStream(String id);
     * </pre>
     *
     * @param contract the service contract to validate
     * @throws Fabric3Exception if the contract is invalid
     */
    private void validateReferenceContract(ServiceContract contract) throws Fabric3Exception {
        if (contract.getOperations().size() != 1) {
            throw new Fabric3Exception("File transfer binding contracts must contain one operation of the form openStream(String id)");
        }
        Operation operation = contract.getOperations().get(0);
        DataType dataType = operation.getInputTypes().get(0);
        if (!(dataType instanceof JavaType)) {
            throw new Fabric3Exception("Unsupported parameter type on binding contract: " + dataType);
        }
        JavaType javaType = (JavaType) dataType;
        if (!(String.class.isAssignableFrom(javaType.getType()))) {
            throw new Fabric3Exception("Parameter type on binding contract must be a string: " + dataType);
        }

        DataType outputType = operation.getOutputType();
        if (!(outputType instanceof JavaType)) {
            throw new Fabric3Exception("Unsupported output type on binding contract: " + outputType);
        }
        JavaType javaOutputType = (JavaType) outputType;
        if (!(OutputStream.class.isAssignableFrom(javaOutputType.getType()))) {
            throw new Fabric3Exception("Output type on binding contract must be a java.io.OutputStream: " + dataType);
        }
    }

    /**
     * Validates a contract for a bound service. The service contract must contain exactly one operation.
     *
     * @param contract the service contract to validate
     * @throws Fabric3Exception if the contract is invalid
     */
    private void validateServiceContract(ServiceContract contract) throws Fabric3Exception {
        if (contract.getOperations().size() > 1 || contract.getOperations().isEmpty()) {
            throw new Fabric3Exception("File transfer binding contracts must contain one operation");
        }
    }

    /**
     * Determines if the contract takes the Java Activation Framework {@link DataHandler} type as a parameter.
     *
     * @param contract the contract
     * @return true if the contract takes the Java Activation Framework {@link DataHandler} type as a parameter
     */
    private boolean isDataHandler(ServiceContract contract) {
        for (Operation operation : contract.getOperations()) {
            for (DataType dataType : operation.getInputTypes()) {
                if (DataHandler.class.isAssignableFrom(dataType.getType())) {
                    return true;
                }
            }
        }
        return false;
    }

    private URI getAdaptorUri(FileBinding definition) throws Fabric3Exception {
        String uri = definition.getAdapterUri();
        if (uri == null) {
            return null;
        }
        try {
            return new URI(info.getDomain().toString() + "/" + uri);
        } catch (URISyntaxException e) {
            throw new Fabric3Exception(e);
        }
    }

}
