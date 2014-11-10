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
package org.fabric3.fabric.container.executor;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.fabric.container.command.ProvisionExtensionsCommand;
import org.fabric3.api.host.contribution.ContributionService;
import org.fabric3.api.host.contribution.ContributionSource;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.spi.container.executor.CommandExecutorRegistry;
import org.fabric3.spi.contribution.ContributionResolver;
import org.oasisopen.sca.annotation.EagerInit;

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
