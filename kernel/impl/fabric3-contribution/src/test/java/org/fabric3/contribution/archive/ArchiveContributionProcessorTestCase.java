/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.contribution.archive;

import java.net.URI;
import java.net.URL;
import java.util.Collections;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.host.stream.Source;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class ArchiveContributionProcessorTestCase extends TestCase {
    private ArchiveContributionProcessor processor;

    public void testStartStop() throws Exception {
        ProcessorRegistry registry = EasyMock.createMock(ProcessorRegistry.class);
        registry.register(EasyMock.isA(ContributionProcessor.class));
        registry.unregister(EasyMock.isA(ContributionProcessor.class));
        EasyMock.replay(registry);

        processor.setContributionProcessorRegistry(registry);

        processor.start();
        processor.stop();
        EasyMock.verify(registry);
    }

    public void testCanProcess() throws Exception {
        ArchiveContributionHandler handler = EasyMock.createMock(ArchiveContributionHandler.class);
        EasyMock.expect(handler.canProcess(EasyMock.isA(Contribution.class))).andReturn(true);
        EasyMock.expect(handler.canProcess(EasyMock.isA(Contribution.class))).andReturn(false);
        EasyMock.replay(handler);

        processor.setHandlers(Collections.<ArchiveContributionHandler>singletonList(handler));

        Contribution contribution1 = new Contribution(URI.create("contribution1"));
        Contribution contribution2 = new Contribution(URI.create("contribution2"));

        assertTrue(processor.canProcess(contribution1));
        assertFalse(processor.canProcess(contribution2));

        EasyMock.verify(handler);
    }

    public void testProcessManifest() throws Exception {
        ArchiveContributionHandler handler = EasyMock.createMock(ArchiveContributionHandler.class);
        EasyMock.expect(handler.canProcess(EasyMock.isA(Contribution.class))).andReturn(true);
        handler.processManifest(EasyMock.isA(Contribution.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(handler);

        processor.setHandlers(Collections.<ArchiveContributionHandler>singletonList(handler));

        Contribution contribution = new Contribution(URI.create("contribution1"));

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        processor.processManifest(contribution, context);

        EasyMock.verify(handler);
    }

    public void testIndex() throws Exception {
        ArchiveContributionHandler handler = EasyMock.createMock(ArchiveContributionHandler.class);
        EasyMock.expect(handler.canProcess(EasyMock.isA(Contribution.class))).andReturn(true);
        handler.iterateArtifacts(EasyMock.isA(Contribution.class), EasyMock.isA(Action.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Contribution contribution = (Contribution) EasyMock.getCurrentArguments()[0];
                Action action = (Action) EasyMock.getCurrentArguments()[1];
                action.process(contribution, "application/xml", new URL("file://test"));
                return null;
            }
        });

        ProcessorRegistry registry = EasyMock.createMock(ProcessorRegistry.class);
        registry.indexResource(EasyMock.isA(Contribution.class),
                               EasyMock.isA(String.class),
                               EasyMock.isA(Source.class),
                               EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(handler, registry);

        processor.setContributionProcessorRegistry(registry);
        processor.setHandlers(Collections.<ArchiveContributionHandler>singletonList(handler));

        Contribution contribution = new Contribution(URI.create("contribution1"));

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();
        processor.index(contribution, context);

        EasyMock.verify(handler, registry);
    }

    public void testProcess() throws Exception {
        ProcessorRegistry registry = EasyMock.createMock(ProcessorRegistry.class);
        registry.processResource(EasyMock.isA(Resource.class), EasyMock.isA(IntrospectionContext.class));
        EasyMock.replay(registry);

        processor.setContributionProcessorRegistry(registry);

        Contribution contribution = new Contribution(URI.create("contribution1"));
        Resource resource = new Resource(contribution, null, "application/xml");
        contribution.addResource(resource);
        
        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(contribution, context);

        EasyMock.verify(registry);
    }

    public void testAlreadyProcessed() throws Exception {
        ProcessorRegistry registry = EasyMock.createMock(ProcessorRegistry.class);
        EasyMock.replay(registry);

        processor.setContributionProcessorRegistry(registry);

        Contribution contribution = new Contribution(URI.create("contribution1"));
        Resource resource = new Resource(contribution, null, "application/xml");
        resource.setState(ResourceState.PROCESSED);
        contribution.addResource(resource);

        DefaultIntrospectionContext context = new DefaultIntrospectionContext();

        processor.process(contribution, context);

        EasyMock.verify(registry);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        processor = new ArchiveContributionProcessor();
    }
}
