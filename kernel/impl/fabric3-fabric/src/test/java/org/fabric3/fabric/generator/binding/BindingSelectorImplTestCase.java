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
package org.fabric3.fabric.generator.binding;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fabric3.fabric.generator.binding.BindingSelectorImpl;
import org.fabric3.fabric.generator.binding.NoSCABindingProviderException;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.binding.provider.BindingMatchResult;
import org.fabric3.spi.binding.provider.BindingProvider;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
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
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER).anyTimes();

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
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER).anyTimes();

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
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER).anyTimes();

        BindingProvider provider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(provider.canBind(EasyMock.isA(LogicalWire.class))).andReturn(NO_MATCH);
        EasyMock.replay(info, provider);

        BindingSelectorImpl selector = new BindingSelectorImpl(info);
        selector.setProviders(Collections.singletonList(provider));

        LogicalCompositeComponent domain = createComponentWithWire("zone1", "zone2");
        try {
            selector.selectBindings(domain);
            fail();
        } catch (NoSCABindingProviderException e) {
            // expected
        }
        EasyMock.verify(info, provider);
    }

    public void testBindChannel() throws Exception {
        HostInfo info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER).anyTimes();

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
        EasyMock.expect(info.getRuntimeMode()).andReturn(RuntimeMode.CONTROLLER).anyTimes();

        BindingProvider provider = EasyMock.createMock(BindingProvider.class);
        EasyMock.expect(provider.canBind(EasyMock.isA(LogicalChannel.class))).andReturn(NO_MATCH);
        EasyMock.replay(info, provider);

        BindingSelectorImpl selector = new BindingSelectorImpl(info);
        selector.setProviders(Collections.singletonList(provider));

        LogicalCompositeComponent domain = createComponentWithChannel();
        try {
            selector.selectBindings(domain);
            fail();
        } catch (NoSCABindingProviderException e) {
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

        ComponentDefinition definition = new ComponentDefinition("target", new Implementation<ComponentType>() {
            public QName getType() {
                return null;
            }
        });
        LogicalComponent target = new LogicalComponent(URI.create("composite/target"), definition, composite);
        target.setZone(targetZone);
        LogicalService service = new LogicalService(URI.create("composite/source#service"), null, target);
        target.addService(service);
        composite.addComponent(target);

        LogicalWire wire = new LogicalWire(composite, reference, service, null);
        composite.addWire(reference, wire);
        return composite;
    }

    private LogicalCompositeComponent createComponentWithChannel() {
        URI compositeUri = URI.create("composite");
        LogicalCompositeComponent composite = new LogicalCompositeComponent(compositeUri, null, null);
        LogicalChannel channel = new LogicalChannel(URI.create("composite/channel"), null, composite);
        composite.addChannel(channel);
        return composite;
    }


}

