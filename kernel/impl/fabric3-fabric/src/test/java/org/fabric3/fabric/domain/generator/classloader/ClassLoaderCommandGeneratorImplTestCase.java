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
package org.fabric3.fabric.domain.generator.classloader;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.contribution.DependencyResolver;
import org.fabric3.fabric.container.command.AttachExtensionCommand;
import org.fabric3.fabric.container.command.ProvisionClassloaderCommand;
import org.fabric3.fabric.container.command.UnprovisionClassloaderCommand;
import org.fabric3.spi.container.command.CompensatableCommand;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionWire;
import org.fabric3.spi.contribution.Export;
import org.fabric3.spi.contribution.Import;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.ClassLoaderWireGenerator;
import org.fabric3.spi.model.physical.PhysicalClassLoaderWireDefinition;

/**
 */
public class ClassLoaderCommandGeneratorImplTestCase extends TestCase {
    private static final URI CONTRIBUTION1 = URI.create("contribution1");
    private static final URI CONTRIBUTION2 = URI.create("contribution2");

    @SuppressWarnings({"unchecked"})
    public void testGenerateWithWire() throws Exception {
        ClassLoaderWireGenerator<MockContributionWire> wireGenerator = EasyMock.createMock(ClassLoaderWireGenerator.class);
        PhysicalClassLoaderWireDefinition definition = new PhysicalClassLoaderWireDefinition(null, null);
        EasyMock.expect(wireGenerator.generate(EasyMock.isA(MockContributionWire.class))).andReturn(definition);
        EasyMock.replay(wireGenerator);
        Map map = Collections.singletonMap(MockContributionWire.class, wireGenerator);
        ClassLoaderCommandGeneratorImpl generator = new ClassLoaderCommandGeneratorImpl(map);

        Map<String, List<Contribution>> contributions = createContributions();
        Map<String, List<CompensatableCommand>> commands = generator.generate(contributions);
        assertEquals(4, commands.get("zone1").size());
        for (CompensatableCommand entry : commands.get("zone1")) {
            if (!(entry instanceof AttachExtensionCommand) && !(entry instanceof ProvisionClassloaderCommand)) {
                fail("Invalid command generated: " + entry.getClass());
            }
        }
        EasyMock.verify(wireGenerator);
    }

    @SuppressWarnings({"unchecked"})
    public void testReleaseWithWire() throws Exception {
        Map<String, List<Contribution>> contributions = createContributions();

        ClassLoaderWireGenerator<MockContributionWire> wireGenerator = EasyMock.createMock(ClassLoaderWireGenerator.class);
        PhysicalClassLoaderWireDefinition definition = new PhysicalClassLoaderWireDefinition(null, null);
        EasyMock.expect(wireGenerator.generate(EasyMock.isA(MockContributionWire.class))).andReturn(definition);

        DependencyResolver dependencyResolver = EasyMock.createMock(DependencyResolver.class);
        EasyMock.expect(dependencyResolver.resolve(EasyMock.isA(List.class))).andReturn(contributions.get("zone1"));

        EasyMock.replay(wireGenerator, dependencyResolver);

        Map map = Collections.singletonMap(MockContributionWire.class, wireGenerator);
        ClassLoaderCommandGeneratorImpl generator = new ClassLoaderCommandGeneratorImpl(map);
        generator.setDependencyService(dependencyResolver);

        Map<String, List<CompensatableCommand>> commands = generator.release(contributions);
        assertEquals(2, commands.get("zone1").size());
        // Do not test for DetachExtensionCommands as they are not generated (they are not necessary as the classloader will be disposed and GCed
        // if its in-use count drops to 0.
        for (CompensatableCommand entry : commands.get("zone1")) {
            if (!(entry instanceof UnprovisionClassloaderCommand)) {
                fail("Invalid command generated: " + entry.getClass());
            }
        }
        EasyMock.verify(wireGenerator, dependencyResolver);
    }

    private Map<String, List<Contribution>> createContributions() {
        Map<String, List<Contribution>> contributions = new HashMap<>();
        Contribution contribution2 = new Contribution(CONTRIBUTION2);
        Contribution contribution1 = new Contribution(CONTRIBUTION1);
        contribution1.addWire(new MockContributionWire());

        contribution1.addResolvedExtensionProvider(URI.create("extension"));
        contribution2.addResolvedExtensionProvider(URI.create("extension"));

        List<Contribution> zone1 = new ArrayList<>();
        zone1.add(contribution2);
        zone1.add(contribution1);
        contributions.put("zone1", zone1);
        return contributions;
    }

    private class MockContributionWire implements ContributionWire {
        private static final long serialVersionUID = -8513574148912964583L;

        public Import getImport() {
            return null;
        }

        public Export getExport() {
            return null;
        }

        public URI getImportContributionUri() {
            return null;
        }

        public URI getExportContributionUri() {
            return null;
        }

        public boolean resolves(Symbol resource) {
            return false;
        }
    }


}