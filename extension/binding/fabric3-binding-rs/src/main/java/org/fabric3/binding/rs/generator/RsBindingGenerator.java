/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.rs.generator;

import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.binding.rs.model.RsBindingDefinition;
import org.fabric3.binding.rs.provision.AuthenticationType;
import org.fabric3.binding.rs.provision.RsSourceDefinition;
import org.fabric3.binding.rs.provision.RsTargetDefinition;
import org.fabric3.host.Namespaces;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 * Implementation of the REST binding generator.
 */
@EagerInit
public class RsBindingGenerator implements BindingGenerator<RsBindingDefinition> {
    private static final QName F3_AUTHORIZATION = new QName(Namespaces.F3, "authorization");
    private static final QName SCA_AUTHORIZATION = new QName(Constants.SCA_NS, "authorization");
    private static final QName SCA_AUTHENTICATION = new QName(Constants.SCA_NS, "clientAuthentication");
    private static final QName F3_BASIC_AUTHENTICATION = new QName(Namespaces.F3, "clientAuthentication");
    private static final QName F3_DIGEST_AUTHENTICATION = null;

    public RsSourceDefinition generateSource(LogicalBinding<RsBindingDefinition> binding,
                                             ServiceContract contract,
                                             List<LogicalOperation> operations,
                                             EffectivePolicy policy) throws GenerationException {
        String interfaze = contract.getQualifiedInterfaceName();
        URI uri = binding.getDefinition().getTargetUri();

        AuthenticationType type = calculateAuthenticationType(binding, operations);
        return new RsSourceDefinition(interfaze, uri, type);
    }

    public RsTargetDefinition generateTarget(LogicalBinding<RsBindingDefinition> binding,
                                             ServiceContract contract,
                                             List<LogicalOperation> operations,
                                             EffectivePolicy policy) throws GenerationException {
        return new RsTargetDefinition(binding.getDefinition().getTargetUri(), contract.getQualifiedInterfaceName());
    }

    public PhysicalTargetDefinition generateServiceBindingTarget(LogicalBinding<RsBindingDefinition> binding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {
        return generateTarget(binding, contract, operations, policy);
    }

    private AuthenticationType calculateAuthenticationType(LogicalBinding<RsBindingDefinition> binding, List<LogicalOperation> operations) {
        if (binding.getIntents().contains(SCA_AUTHENTICATION)) {
            return AuthenticationType.BASIC;
        } else if (binding.getIntents().contains(F3_BASIC_AUTHENTICATION)) {
            return AuthenticationType.BASIC;
        } else if (binding.getIntents().contains(F3_DIGEST_AUTHENTICATION)) {
            return AuthenticationType.BASIC;
        }
        Set<QName> intents = binding.getParent().getIntents();
        if (intents.contains(F3_AUTHORIZATION) || intents.contains(SCA_AUTHORIZATION)) {
            return AuthenticationType.BASIC;
        }
        intents = binding.getParent().getParent().getDefinition().getImplementation().getIntents();
        if (intents.contains(F3_AUTHORIZATION) || intents.contains(SCA_AUTHORIZATION)) {
            return AuthenticationType.BASIC;
        }
        for (LogicalOperation operation : operations) {
            intents = operation.getIntents();
            if (intents.contains(F3_AUTHORIZATION) || intents.contains(SCA_AUTHORIZATION)) {
                // default to basic authentication
                return AuthenticationType.BASIC;
            }
        }

        return AuthenticationType.NONE;
    }

}
