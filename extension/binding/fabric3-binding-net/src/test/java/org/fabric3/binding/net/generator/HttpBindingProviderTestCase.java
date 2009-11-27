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
package org.fabric3.binding.net.generator;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.binding.net.model.HttpBindingDefinition;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.topology.DomainManager;

/**
 * @version $Rev$ $Date$
 */
public class HttpBindingProviderTestCase extends TestCase {
    private HttpBindingProvider bindingProvider;

    public void testGenerateServiceAndReference() throws Exception {
        JavaServiceContract contract = new JavaServiceContract();
        JavaServiceContract callbackContract = new JavaServiceContract();
        contract.setCallbackContract(callbackContract);

        LogicalComponent<?> source = new LogicalComponent(URI.create("fabric3://runtime/source"), null, null);
        source.setZone("zone1");
        LogicalComponent<?> target = new LogicalComponent(URI.create("fabric3://runtime/source"), null, null);
        target.setZone("zone2");
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("fabric3://runtime/source#reference"), referenceDefinition, source);
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("fabric3://runtime/source#service"), serviceDefinition, target);
        bindingProvider.bind(reference, service);

        // verify reference
        LogicalBinding generatedReference = reference.getBindings().get(0);
        HttpBindingDefinition generatedReferenceBinding = (HttpBindingDefinition) generatedReference.getDefinition();
        assertEquals("http://localhost:8082/source/service", generatedReferenceBinding.getTargetUri().toString());

        // verify reference callback
        LogicalBinding generatedCallbackReference = reference.getCallbackBindings().get(0);
        HttpBindingDefinition generatedReferenceCallbackBinding = (HttpBindingDefinition) generatedCallbackReference.getDefinition();
        assertEquals("/source/reference", generatedReferenceCallbackBinding.getTargetUri().toString());

        // verify service
        LogicalBinding generatedService = service.getBindings().get(0);
        HttpBindingDefinition generatedServiceBinding = (HttpBindingDefinition) generatedService.getDefinition();
        assertEquals("/source/service", generatedServiceBinding.getTargetUri().toString());

        // verify service callback
        LogicalBinding generatedCallbackService = service.getCallbackBindings().get(0);
        HttpBindingDefinition generatedCallbackServiceBinding = (HttpBindingDefinition) generatedCallbackService.getDefinition();
        assertEquals("http://localhost:8081/source/reference", generatedCallbackServiceBinding.getTargetUri().toString());

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DomainManager manager = EasyMock.createMock(DomainManager.class);
        manager.getTransportMetaData("zone2", String.class, "binding.net.http");
        EasyMock.expectLastCall().andReturn("localhost:8082");
        manager.getTransportMetaData("zone1", String.class, "binding.net.http");
        EasyMock.expectLastCall().andReturn("localhost:8081");
        EasyMock.replay(manager);
        bindingProvider = new HttpBindingProvider();
        bindingProvider.setDomainManager(manager);
    }

}
