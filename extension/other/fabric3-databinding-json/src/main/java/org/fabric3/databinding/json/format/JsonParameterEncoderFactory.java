/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.databinding.json.format;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.binding.format.ParameterEncoderFactory;
import org.fabric3.spi.model.physical.ParameterTypeHelper;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Creates JsonParameterEncoder instances.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JsonParameterEncoderFactory implements ParameterEncoderFactory {

    public ParameterEncoder getInstance(Wire wire, ClassLoader loader) throws EncoderException {
        Map<String, OperationTypes> mappings = new HashMap<String, OperationTypes>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition definition = chain.getPhysicalOperation();
            String name = definition.getName();
            Set<Class<?>> inParams;
            try {
                inParams = ParameterTypeHelper.loadSourceInParameterTypes(definition, loader);
            } catch (ClassNotFoundException e) {
                throw new EncoderException(e);
            }
            if (inParams.size() > 1) {
                throw new EncoderException("Multiple parameters not supported");
            }

            Class<?> inParam;
            if (inParams.isEmpty()) {
                inParam = null;
            } else {
                inParam = inParams.iterator().next();
            }
            try {
                Class<?> outParam = ParameterTypeHelper.loadTargetOutputType(definition, loader);
                Set<Class<?>> faults = ParameterTypeHelper.loadSourceFaultTypes(definition, loader);
                OperationTypes types = new OperationTypes(inParam, outParam, faults);
                mappings.put(name, types);
            } catch (ClassNotFoundException e) {
                throw new EncoderException(e);
            }
        }
        return new JsonParameterEncoder(mappings);
    }

}