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
package org.fabric3.binding.ftp.generator;

import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.binding.ftp.provision.FtpWireTargetDefinition;
import org.oasisopen.sca.annotation.Property;
import org.w3c.dom.Element;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.binding.ftp.model.FtpBindingDefinition;
import org.fabric3.binding.ftp.model.TransferMode;
import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.binding.ftp.provision.FtpWireSourceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 *
 */
public class FtpWireBindingGenerator implements WireBindingGenerator<FtpBindingDefinition> {
    private int connectTimeout = 120000; // two minutes
    private int socketTimeout = 1800000;  // default timeout of 30 minutes

    /**
     * Optionally configures a timeout setting for openning a socket connection. The default wait is 2 minutes.
     *
     * @param connectTimeout the timeout in milliseconds
     */
    @Property(required = false)
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Optionally configures a timeout setting for socket connections from the client to a server. The default is 30 minutes.
     *
     * @param socketTimeout the timeout in milliseconds
     */
    @Property(required = false)
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public FtpWireSourceDefinition generateSource(LogicalBinding<FtpBindingDefinition> binding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        if (contract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        FtpWireSourceDefinition hwsd = new FtpWireSourceDefinition();
        URI targetUri = binding.getDefinition().getTargetUri();
        hwsd.setUri(targetUri);

        return hwsd;

    }

    public FtpWireTargetDefinition generateTarget(LogicalBinding<FtpBindingDefinition> binding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        if (operations.size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        URI id = binding.getParent().getParent().getParent().getUri();
        FtpBindingDefinition definition = binding.getDefinition();
        boolean active = definition.getTransferMode() == TransferMode.ACTIVE;

        FtpSecurity security = processPolicies(policy);

        FtpWireTargetDefinition hwtd = new FtpWireTargetDefinition(id, active, security, connectTimeout, socketTimeout);
        hwtd.setUri(definition.getTargetUri());
        if (!definition.getSTORCommands().isEmpty()) {
            hwtd.setSTORCommands(definition.getSTORCommands());
        }
        hwtd.setTmpFileSuffix(binding.getDefinition().getTmpFileSuffix());
        return hwtd;

    }

    public PhysicalWireTargetDefinition generateServiceBindingTarget(LogicalBinding<FtpBindingDefinition> serviceBinding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    private FtpSecurity processPolicies(EffectivePolicy policy) throws GenerationException {

        Set<PolicySet> policySets = policy.getEndpointPolicySets();
        if (policySets == null || policySets.size() == 0) {
            return null;
        }
        if (policySets.size() != 1) {
            throw new GenerationException("Invalid policy configuration, only supports security policy");
        }

        PolicySet policySet = policySets.iterator().next();

        QName policyQName = policySet.getExpressionName();
        if (!policyQName.equals(Constants.POLICY_QNAME)) {
            throw new GenerationException("Unexpected policy element " + policyQName);
        }

        Element expression = policySet.getExpression();
        String user = expression.getAttribute("user");
        if (user == null) {
            throw new GenerationException("User name not specified in security policy");
        }
        String password = expression.getAttribute("password");
        if (password == null) {
            throw new GenerationException("Password not specified in security policy");
        }

        return new FtpSecurity(user, password);

    }

}
