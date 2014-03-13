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

import javax.activation.DataHandler;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.binding.file.model.FileBindingDefinition;
import org.fabric3.binding.file.provision.FileBindingSourceDefinition;
import org.fabric3.binding.file.provision.FileBindingTargetDefinition;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.deployment.generator.binding.BindingGenerator;
import org.fabric3.spi.deployment.generator.policy.EffectivePolicy;
import org.fabric3.spi.deployment.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.type.java.JavaType;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 *
 */
@EagerInit
public class FileBindingGenerator implements BindingGenerator<FileBindingDefinition> {
    private static final String REGEX_ALL = ".*";
    private HostInfo info;

    private long defaultDelay = 2000;

    @Property(required = false)
    public void setDelay(long delay) {
        this.defaultDelay = delay;
    }

    public FileBindingGenerator(@Reference HostInfo info) {
        this.info = info;
    }

    public FileBindingSourceDefinition generateSource(LogicalBinding<FileBindingDefinition> binding,
                                                      ServiceContract contract,
                                                      List<LogicalOperation> operations,
                                                      EffectivePolicy policy) throws GenerationException {
        validateServiceContract(contract);
        boolean dataHandler = isDataHandler(contract);
        FileBindingDefinition definition = binding.getDefinition();
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
            throw new GenerationException("Error location must be specified on the file binding configuration for " + uri);
        }
        String adapterClass = definition.getAdapterClass();
        URI adaptorUri = getAdaptorUri(definition);

        long delay = definition.getDelay();
        if (delay == -1) {
            delay = defaultDelay;
        }
        return new FileBindingSourceDefinition(uri, pattern, location, strategy, archiveLocation, errorLocation, adapterClass, adaptorUri, delay, dataHandler);
    }

    public FileBindingTargetDefinition generateTarget(LogicalBinding<FileBindingDefinition> binding,
                                                      ServiceContract contract,
                                                      List<LogicalOperation> operations,
                                                      EffectivePolicy policy) throws GenerationException {
        validateReferenceContract(contract);
        FileBindingDefinition definition = binding.getDefinition();
        String location = definition.getLocation();
        String adapterClass = definition.getAdapterClass();
        URI adaptorUri = getAdaptorUri(definition);
        return new FileBindingTargetDefinition(location, adapterClass, adaptorUri);
    }

    public PhysicalTargetDefinition generateServiceBindingTarget(LogicalBinding<FileBindingDefinition> binding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Validates a service contract for a bound reference. The service contract must contain exactly one operation of the form:
     * <pre>
     * OutputStream openStream(String id);
     * </pre>
     *
     * @param contract the service contract to validate
     * @throws InvalidContractException if the contract is invalid
     */
    private void validateReferenceContract(ServiceContract contract) throws InvalidContractException {
        if (contract.getOperations().size() != 1) {
            throw new InvalidContractException("File transfer binding contracts must contain one operation of the form openStream(String id)");
        }
        Operation operation = contract.getOperations().get(0);
        DataType dataType = operation.getInputTypes().get(0);
        if (!(dataType instanceof JavaType)) {
            throw new InvalidContractException("Unsupported parameter type on binding contract: " + dataType);
        }
        JavaType<?> javaType = (JavaType) dataType;
        if (!(String.class.isAssignableFrom(javaType.getPhysical()))) {
            throw new InvalidContractException("Parameter type on binding contract must be a string: " + dataType);
        }

        DataType outputType = operation.getOutputType();
        if (!(outputType instanceof JavaType)) {
            throw new InvalidContractException("Unsupported output type on binding contract: " + outputType);
        }
        JavaType<?> javaOutputType = (JavaType) outputType;
        if (!(OutputStream.class.isAssignableFrom(javaOutputType.getPhysical()))) {
            throw new InvalidContractException("Output type on binding contract must be a java.io.OutputStream: " + dataType);
        }
    }

    /**
     * Validates a contract for a bound service. The service contract must contain exactly one operation.
     *
     * @param contract the service contract to validate
     * @throws InvalidContractException if the contract is invalid
     */
    private void validateServiceContract(ServiceContract contract) throws InvalidContractException {
        if (contract.getOperations().size() > 1 || contract.getOperations().isEmpty()) {
            throw new InvalidContractException("File transfer binding contracts must contain one operation");
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
                if (DataHandler.class.isAssignableFrom(dataType.getPhysical())) {
                    return true;
                }
            }
        }
        return false;
    }

    private URI getAdaptorUri(FileBindingDefinition definition) throws GenerationException {
        String uri = definition.getAdapterUri();
        if (uri == null) {
            return null;
        }
        try {
            return new URI(info.getDomain().toString() + "/" + uri);
        } catch (URISyntaxException e) {
            throw new GenerationException(e);
        }
    }

}
