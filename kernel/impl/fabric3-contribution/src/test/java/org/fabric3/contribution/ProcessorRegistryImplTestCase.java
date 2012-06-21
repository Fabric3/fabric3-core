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
package org.fabric3.contribution;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class ProcessorRegistryImplTestCase extends TestCase {
    private ProcessorRegistryImpl registry;
    private IntrospectionContext context;
    private Contribution contribution;
    private Resource resource;

    public void testRegisterUnRegisterContributionProcessor() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.replay(processor);

        registry.register(processor);
        registry.unregister(processor);

        EasyMock.verify(processor);
    }

    public void testRegisterUnRegisterResourceProcessor() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("application/xml");
        EasyMock.replay(processor);

        registry.register(processor);

        registry.unregister("application/xml");

        EasyMock.verify(processor);
    }

    public void testProcessManifest() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.expect(processor.canProcess(contribution)).andReturn(true);
        processor.processManifest(contribution, context);
        EasyMock.replay(processor);

        registry.register(processor);

        registry.processManifest(contribution, context);
        EasyMock.verify(processor);
    }

    public void testIndexContribution() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.expect(processor.canProcess(contribution)).andReturn(true);
        processor.index(contribution, context);
        EasyMock.replay(processor);

        registry.register(processor);

        registry.indexContribution(contribution, context);
        EasyMock.verify(processor);
    }

    public void testProcessContribution() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.expect(processor.canProcess(contribution)).andReturn(true);
        processor.process(contribution, context);
        EasyMock.replay(processor);

        registry.register(processor);

        registry.processContribution(contribution, context);
        EasyMock.verify(processor);
    }

    public void testProcessContributionNoProcessor() throws Exception {
        ContributionProcessor processor = EasyMock.createMock(ContributionProcessor.class);
        EasyMock.expect(processor.canProcess(contribution)).andReturn(false);
        EasyMock.replay(processor);

        registry.register(processor);

        try {
            registry.processContribution(contribution, context);
            fail();
        } catch (InstallException e) {
            // expected
        }
        EasyMock.verify(processor);

    }

    public void testIndexResource() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("application/xml");
        processor.index(EasyMock.isA(Resource.class), EasyMock.eq(context));
        EasyMock.replay(processor);

        registry.register(processor);

        registry.indexResource(contribution,"application/xml", null, context);
        EasyMock.verify(processor);
    }

    public void testIndexResourceUnknownType() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("text/plain");
        EasyMock.replay(processor);

        registry.register(processor);

        registry.indexResource(contribution,"application/xml", null, context);
        EasyMock.verify(processor);
    }

    public void testProcessResource() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("application/xml");
        processor.process(resource, context);
        EasyMock.replay(processor);

        registry.register(processor);

        registry.processResource(resource, context);
        EasyMock.verify(processor);
    }

    public void testProcessResourceUnknownType() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("text/plain");
        EasyMock.replay(processor);

        registry.register(processor);

        registry.processResource(resource, context);
        EasyMock.verify(processor);
    }

    public void testDoNotProcessResourceInError() throws Exception {
        ResourceProcessor processor = EasyMock.createMock(ResourceProcessor.class);
        EasyMock.expect(processor.getContentType()).andReturn("application/xml");
        EasyMock.replay(processor);
        resource.setState(ResourceState.ERROR);

        registry.register(processor);

        registry.processResource(resource, context);
        EasyMock.verify(processor);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registry = new ProcessorRegistryImpl();
        context = new DefaultIntrospectionContext();
        contribution = new Contribution(URI.create("contribution"));
        resource = new Resource(contribution, null, "application/xml");
    }
}
