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