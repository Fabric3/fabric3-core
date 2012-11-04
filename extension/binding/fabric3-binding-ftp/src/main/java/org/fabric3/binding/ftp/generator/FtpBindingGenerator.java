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
package org.fabric3.binding.ftp.generator;

import java.net.URI;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Property;
import org.w3c.dom.Element;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.binding.ftp.model.FtpBindingDefinition;
import org.fabric3.binding.ftp.model.TransferMode;
import org.fabric3.binding.ftp.provision.FtpSecurity;
import org.fabric3.binding.ftp.provision.FtpSourceDefinition;
import org.fabric3.binding.ftp.provision.FtpTargetDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;

/**
 *
 */
public class FtpBindingGenerator implements BindingGenerator<FtpBindingDefinition> {
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

    public FtpSourceDefinition generateSource(LogicalBinding<FtpBindingDefinition> binding,
                                              ServiceContract contract,
                                              List<LogicalOperation> operations,
                                              EffectivePolicy policy) throws GenerationException {

        if (contract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        FtpSourceDefinition hwsd = new FtpSourceDefinition();
        URI targetUri = binding.getDefinition().getTargetUri();
        hwsd.setUri(targetUri);

        return hwsd;

    }

    public FtpTargetDefinition generateTarget(LogicalBinding<FtpBindingDefinition> binding,
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

        FtpTargetDefinition hwtd = new FtpTargetDefinition(id, active, security, connectTimeout, socketTimeout);
        hwtd.setUri(definition.getTargetUri());
        if (!definition.getSTORCommands().isEmpty()) {
            hwtd.setSTORCommands(definition.getSTORCommands());
        }
        hwtd.setTmpFileSuffix(binding.getDefinition().getTmpFileSuffix());
        return hwtd;

    }

    public PhysicalTargetDefinition generateServiceBindingTarget(LogicalBinding<FtpBindingDefinition> serviceBinding,
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
