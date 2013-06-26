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
package org.fabric3.fabric.executor;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.oasisopen.sca.annotation.EagerInit;

import org.fabric3.fabric.command.ProvisionExtensionsCommand;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.domain.Domain;
import org.fabric3.spi.contribution.ContributionResolver;
import org.fabric3.spi.executor.CommandExecutorRegistry;

/**
 *
 */
@EagerInit
public class ProvisionExtensionsCommandExecutorTestCase extends TestCase {
    private CommandExecutorRegistry executorRegistry;
    private ContributionService contributionService;
    private Domain domain;
    private ContributionResolver resolver;
    private ProvisionedExtensionTracker tracker;
    private ProvisionExtensionsCommandExecutor executor;

    @SuppressWarnings({"unchecked"})
    public void testProvision() throws Exception {
        URI contribution1Uri = URI.create("contribution1");
        URI contribution2Uri = URI.create("contribution2");

        EasyMock.expect(contributionService.exists(contribution1Uri)).andReturn(false);
        EasyMock.expect(contributionService.exists(contribution2Uri)).andReturn(true);
        EasyMock.expect(contributionService.store(EasyMock.isA(ContributionSource.class))).andReturn(contribution1Uri);
        tracker.increment(contribution1Uri);
        EasyMock.expect(contributionService.install(EasyMock.isA(List.class))).andReturn(Collections.singletonList(contribution1Uri));
        EasyMock.expect(resolver.resolve(contribution1Uri)).andReturn(new URL("file://foo"));
        domain.include(EasyMock.isA(List.class));

        EasyMock.replay(executorRegistry, contributionService, domain, resolver, tracker);

        ProvisionExtensionsCommand command = new ProvisionExtensionsCommand();
        command.addExtensionUri(contribution1Uri);
        command.addExtensionUri(contribution2Uri);
        
        executor.execute(command);

        EasyMock.verify(executorRegistry, contributionService, domain, resolver, tracker);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        executorRegistry = EasyMock.createMock(CommandExecutorRegistry.class);
        contributionService = EasyMock.createMock(ContributionService.class);
        domain = EasyMock.createMock(Domain.class);
        resolver = EasyMock.createMock(ContributionResolver.class);
        tracker = EasyMock.createMock(ProvisionedExtensionTracker.class);

        executor = new ProvisionExtensionsCommandExecutor(domain, executorRegistry, contributionService, resolver, tracker);
    }
}
