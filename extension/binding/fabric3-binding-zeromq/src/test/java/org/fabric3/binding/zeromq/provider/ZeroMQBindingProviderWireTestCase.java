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
package org.fabric3.binding.zeromq.provider;

import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.component.ServiceDefinition;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 *
 */
public class ZeroMQBindingProviderWireTestCase extends TestCase {
    private LogicalWire wire;
    private ZeroMQBindingProvider provider;

    public void testCanBind() throws Exception {
        assertTrue(provider.canBind(wire).isMatch());
    }

    public void testBindWire() throws Exception {
        provider.bind(wire);
        LogicalReference source = wire.getSource().getLeafReference();
        LogicalService target = wire.getTarget().getLeafService();

        assertFalse(source.getBindings().isEmpty());
        assertFalse(source.getCallbackBindings().isEmpty());
        assertFalse(target.getBindings().isEmpty());
        assertFalse(target.getCallbackBindings().isEmpty());
    }

    @SuppressWarnings({"unchecked"})
    protected void setUp() {
        provider = new ZeroMQBindingProvider();
        QName deployable = new QName("test", "composite");

        LogicalComponent client = new LogicalComponent(URI.create("client"), null, null);
        client.setDeployable(deployable);
        LogicalComponent component = new LogicalComponent(URI.create("client"), null, null);
        component.setDeployable(deployable);
        ServiceContract contract = new ServiceContract() {
            @Override
            public String getQualifiedInterfaceName() {
                return "service";
            }

            @Override
            public ServiceContract getCallbackContract() {
                return this;
            }
        };
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("domain://client#reference"), referenceDefinition, client);

        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("domain://component#service"), serviceDefinition, component);

        wire = new LogicalWire(null, reference, service, deployable);
    }


}
