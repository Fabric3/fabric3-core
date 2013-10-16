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
package org.fabric3.fabric.deployment.executor;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.deployment.command.UnProvisionExtensionsCommand;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.spi.command.CommandExecutorRegistry;
import org.fabric3.spi.contribution.ContributionResolver;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class UnProvisionExtensionsCommandExecutorTestCase extends TestCase {
    private CommandExecutorRegistry executorRegistry;
    private ContributionService contributionService;
    private Domain domain;
    private ContributionResolver resolver;
    private ProvisionedExtensionTracker tracker;
    private UnProvisionExtensionsCommandExecutor executor;

    @SuppressWarnings({"unchecked"})
    public void testUnProvision() throws Exception {
        URI contribution1Uri = URI.create("contribution1");
        URI contribution2Uri = URI.create("contribution2");

        EasyMock.expect(tracker.decrement(contribution1Uri)).andReturn(0);
        EasyMock.expect(tracker.decrement(contribution2Uri)).andReturn(1);

        domain.undeploy(contribution1Uri, false);
        resolver.release(contribution1Uri);
        contributionService.uninstall(EasyMock.isA(List.class));
        contributionService.remove(Collections.singletonList(contribution1Uri));

        EasyMock.replay(executorRegistry, contributionService, domain, resolver, tracker);

        UnProvisionExtensionsCommand command = new UnProvisionExtensionsCommand();
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

        executor = new UnProvisionExtensionsCommandExecutor(domain, executorRegistry, contributionService, resolver, tracker);
    }
}
