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
 */
package org.fabric3.binding.zeromq.provider;

import javax.xml.namespace.QName;
import java.net.URI;

import junit.framework.TestCase;
import org.fabric3.api.model.type.component.Reference;
import org.fabric3.api.model.type.component.Service;
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
        Reference referenceDefinition = new Reference("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("domain://client#reference"), referenceDefinition, client);

        Service serviceDefinition = new Service("service", contract);
        LogicalService service = new LogicalService(URI.create("domain://component#service"), serviceDefinition, component);

        wire = new LogicalWire(null, reference, service, deployable);
    }


}
