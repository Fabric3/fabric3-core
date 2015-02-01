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
package org.fabric3.fabric.domain.generator.binding;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.api.host.ContainerException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.api.model.type.RuntimeMode;
import org.fabric3.api.model.type.component.Channel;
import org.fabric3.api.model.type.component.Component;
import org.fabric3.api.model.type.component.ComponentType;
import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.component.Service;
import org.fabric3.spi.domain.generator.binding.BindingMatchResult;
import org.fabric3.spi.domain.generator.binding.BindingProvider;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.type.java.JavaServiceContract;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class BindingSelectorImplTestCase extends TestCase {
    private static final BindingMatchResult MATCH = new BindingMatchResult(true, new QName("test", "binding"));
    private static final BindingMatchResult NO_MATCH = new BindingMatchResult(false, new QName("test", "binding"));

    public void testBindWire() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).anyTimes();

        BindingProvider provider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(provider.canBind(EasyMock.isA(LogicalWire.class))).andReturn(MATCH);
        provider.bind(EasyMock.isA(LogicalWire.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable {
                LogicalWire wire = (LogicalWire) EasyMock.getCurrentArguments()[0];
                LogicalReference reference = wire.getSource();
                reference.addBinding(new LogicalBinding(null, reference));
                LogicalService service = wire.getTarget();
                service.addBinding(new LogicalBinding(null, service));
                return null;
            }
        });
        EasyMock.replay(info, provider);

        BindingSelectorImpl selector = new BindingSelectorImpl(info);
        selector.setProviders(Collections.singletonList(provider));

        LogicalCompositeComponent domain = createComponentWithWire("zone1", "zone2");
        selector.selectBindings(domain);

        EasyMock.verify(info, provider);
    }

    public void testSkipLocalWire() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).anyTimes();

        BindingProvider provider = EasyMock.createMock(BindingProvider.class);
        EasyMock.replay(info, provider);

        BindingSelectorImpl selector = new BindingSelectorImpl(info);
        selector.setProviders(Collections.singletonList(provider));

        LogicalCompositeComponent domain = createComponentWithWire("zone1", "zone1");
        selector.selectBindings(domain);

        EasyMock.verify(info, provider);
    }

    public void testNoProviderForWire() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).anyTimes();

        BindingProvider provider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(provider.canBind(EasyMock.isA(LogicalWire.class))).andReturn(NO_MATCH);
        EasyMock.replay(info, provider);

        BindingSelectorImpl selector = new BindingSelectorImpl(info);
        selector.setProviders(Collections.singletonList(provider));

        LogicalCompositeComponent domain = createComponentWithWire("zone1", "zone2");
        try {
            selector.selectBindings(domain);
            fail();
        } catch (ContainerException e) {
            // expected
        }
        EasyMock.verify(info, provider);
    }

    public void testBindChannel() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).anyTimes();

        BindingProvider provider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(provider.canBind(EasyMock.isA(LogicalChannel.class))).andReturn(MATCH);
        provider.bind(EasyMock.isA(LogicalChannel.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {
            @SuppressWarnings({"unchecked"})
            public Object answer() throws Throwable {
                LogicalChannel channel = (LogicalChannel) EasyMock.getCurrentArguments()[0];
                channel.addBinding(new LogicalBinding(null, channel));
                return null;
            }
        });
        EasyMock.replay(info, provider);

        BindingSelectorImpl selector = new BindingSelectorImpl(info);
        selector.setProviders(Collections.singletonList(provider));

        LogicalCompositeComponent domain = createComponentWithChannel();
        selector.selectBindings(domain);

        EasyMock.verify(info, provider);
    }

    public void testNoProviderForChannel() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.NODE).anyTimes();

        BindingProvider provider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(provider.canBind(EasyMock.isA(LogicalChannel.class))).andReturn(NO_MATCH);
        EasyMock.replay(info, provider);

        BindingSelectorImpl selector = new BindingSelectorImpl(info);
        selector.setProviders(Collections.singletonList(provider));

        LogicalCompositeComponent domain = createComponentWithChannel();
        try {
            selector.selectBindings(domain);
            fail();
        } catch (ContainerException e) {
            // expected
        }
        EasyMock.verify(info, provider);
    }

    @SuppressWarnings({"unchecked"})
    private LogicalCompositeComponent createComponentWithWire(String sourceZone, String targetZone) {
        URI compositeUri = URI.create("composite");
        LogicalCompositeComponent composite = new LogicalCompositeComponent(compositeUri, null, null);

        LogicalComponent source = new LogicalComponent(URI.create("composite/source"), null, composite);
        LogicalReference reference = new LogicalReference(URI.create("composite/source#reference"), null, source);
        source.setZone(sourceZone);
        source.addReference(reference);
        composite.addComponent(source);

        Component definition = new Component("target", new Implementation<ComponentType>() {
            public String getType() {
                return null;
            }
        });
        LogicalComponent target = new LogicalComponent(URI.create("composite/target"), definition, composite);
        target.setZone(targetZone);
        Service componentService = new Service("test");
        componentService.setServiceContract(new JavaServiceContract());
        LogicalService service = new LogicalService(URI.create("composite/source#service"), componentService, target);

        target.addService(service);
        composite.addComponent(target);

        LogicalWire wire = new LogicalWire(composite, reference, service, null);
        composite.addWire(reference, wire);
        return composite;
    }

    private LogicalCompositeComponent createComponentWithChannel() {
        URI compositeUri = URI.create("composite");
        LogicalCompositeComponent composite = new LogicalCompositeComponent(compositeUri, null, null);
        Channel definition = new Channel("channel");
        LogicalChannel channel = new LogicalChannel(URI.create("composite/channel"), definition, composite);
        composite.addChannel(channel);
        return composite;
    }

}

