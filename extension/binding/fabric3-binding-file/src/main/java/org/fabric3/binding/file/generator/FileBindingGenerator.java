/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
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

import java.util.List;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.binding.file.model.FileBindingDefinition;
import org.fabric3.binding.file.provision.FileBindingSourceDefinition;
import org.fabric3.binding.file.provision.FileBindingTargetDefinition;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class FileBindingGenerator implements BindingGenerator<FileBindingDefinition> {
    private static final QName IMMEDIATE_ONEWAY = new QName(Constants.SCA_NS, "immediateOneWay");
    private static final QName ONEWAY = new QName(Constants.SCA_NS, "oneWay");

    public FileBindingSourceDefinition generateSource(LogicalBinding<FileBindingDefinition> binding,
                                                      ServiceContract contract,
                                                      List<LogicalOperation> operations,
                                                      EffectivePolicy policy) throws GenerationException {
        validateServiceContract(contract);
        FileBindingDefinition definition = binding.getDefinition();
        String location = definition.getLocation();
        return new FileBindingSourceDefinition(location);
    }

    public FileBindingTargetDefinition generateTarget(LogicalBinding<FileBindingDefinition> binding,
                                                      ServiceContract contract,
                                                      List<LogicalOperation> operations,
                                                      EffectivePolicy policy) throws GenerationException {
        validateReferenceContract(contract);
        FileBindingDefinition definition = binding.getDefinition();
        String location = definition.getLocation();
        return new FileBindingTargetDefinition(location);
    }

    public PhysicalTargetDefinition generateServiceBindingTarget(LogicalBinding<FileBindingDefinition> binding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    private void validateReferenceContract(ServiceContract contract) throws InvalidContractException {
        if (contract.getOperations().size() > 1 || contract.getOperations().isEmpty()) {
            throw new InvalidContractException("File transfer binding contracts must contain one operation of the form openStream(InputStream)");
        }
        Operation operation = contract.getOperations().get(0);
        if (!"openStream".equals(operation.getName())) {
            throw new InvalidContractException("File transfer binding contracts must contain one operation of the form openStream(InputStream)");
        }
        // TODO validate input type is 1, and an input stream
    }

    private void validateServiceContract(ServiceContract contract) throws InvalidContractException {
        if (contract.getOperations().size() > 1 || contract.getOperations().isEmpty()) {
            throw new InvalidContractException("File transfer binding contracts must contain one operation of the form openStream(InputStream)");
        }
        Operation operation = contract.getOperations().get(0);
        // TODO validate input type is 1, and an inputstream
    }

}
