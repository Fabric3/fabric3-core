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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.contribution;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.contribution.ContributionLockedException;
import org.fabric3.host.contribution.ContributionOrder;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.RemoveException;
import org.fabric3.host.contribution.UninstallException;
import org.fabric3.host.repository.Repository;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class ContributionServiceImplTestCase extends TestCase {
    private ProcessorRegistry processorRegistry;
    private MetaDataStore store;
    private ContributionLoader loader;
    private ContentTypeResolver resolver;
    private DependencyService dependencyService;

    private URI contributionUri;
    private QName deployableName;
    private Deployable deployable;
    private Contribution contribution;

    private ContributionServiceImpl service;
    private URI profileUri;

    public void testExists() throws Exception {
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        assertTrue(service.exists(contributionUri));

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testGetContributions() throws Exception {
        EasyMock.expect(store.getContributions()).andReturn(Collections.singleton(contribution));
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        assertTrue(service.getContributions().contains(contributionUri));

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testGetTimeStamp() throws Exception {
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        assertEquals(1, service.getContributionTimestamp(contributionUri));

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testGetDeployables() throws Exception {
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        assertTrue(service.getDeployables(contributionUri).contains(deployable));

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testGetDeployedComposites() throws Exception {
        contribution.acquireLock(deployable.getName());
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        assertTrue(service.getDeployedComposites(contributionUri).contains(deployable.getName()));

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testStore() throws Exception {
        EasyMock.expect(store.find(contributionUri)).andReturn(null);
        store.store(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        ContributionSource source = new FileContributionSource(contributionUri, null, -1, "application/xml", false);
        assertEquals(contributionUri, service.store(source));

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testStoreMultiple() throws Exception {
        EasyMock.expect(store.find(contributionUri)).andReturn(null);
        store.store(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        ContributionSource source = new FileContributionSource(contributionUri, null, -1, "application/xml", false);
        assertTrue(service.store(Collections.singletonList(source)).contains(contributionUri));

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testInstall() throws Exception {
        createResourceWithComposite();

        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        processorRegistry.processManifest(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expect(dependencyService.order(EasyMock.isA(List.class))).andReturn(Collections.singletonList(contribution));
        EasyMock.expect(loader.load(contribution)).andReturn(getClass().getClassLoader());
        processorRegistry.indexContribution(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));
        store.store(contribution);
        processorRegistry.processContribution(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        service.install(contributionUri);

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testUnInstall() throws Exception {
        contribution.setState(ContributionState.INSTALLED);
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        loader.unload(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        service.uninstall(contributionUri);

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testUnInstallMultiple() throws Exception {
        contribution.setState(ContributionState.INSTALLED);
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.expect(dependencyService.orderForUninstall(EasyMock.isA(List.class))).andReturn(Collections.singletonList(contribution));
        loader.unload(contribution);

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        service.uninstall(Collections.singletonList(contributionUri));

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testUnInstallContributionLocked() throws Exception {
        contribution.setState(ContributionState.INSTALLED);
        contribution.acquireLock(deployableName);
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);
        try {
            service.uninstall(contributionUri);
            fail();
        } catch (ContributionLockedException e) {
            // expected
        }
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testUnInstallContributionNotInstalled() throws Exception {
        contribution.acquireLock(deployableName);
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);
        try {
            service.uninstall(contributionUri);
            fail();
        } catch (UninstallException e) {
            // expected
        }
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testRemoveContribution() throws Exception {
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        store.remove(contributionUri);
        Repository repository = EasyMock.createMock(Repository.class);
        repository.remove(contributionUri);
        service.setRepository(repository);

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService, repository);
        service.remove(contributionUri);
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService, repository);
    }

    public void testRemoveContributionsMultiple() throws Exception {
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        store.remove(contributionUri);
        Repository repository = EasyMock.createMock(Repository.class);
        repository.remove(contributionUri);
        service.setRepository(repository);

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService, repository);
        service.remove(Collections.singletonList(contributionUri));
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService, repository);
    }

    public void testProfileExists() throws Exception {
        EasyMock.expect(store.getContributions()).andReturn(Collections.<Contribution>singleton(contribution));

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        assertTrue(service.profileExists(profileUri));
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testGetContributionsInProfile() throws Exception {
        EasyMock.expect(store.getContributions()).andReturn(Collections.<Contribution>singleton(contribution));

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        assertTrue(service.getContributionsInProfile(profileUri).contains(contribution.getUri()));
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testGetSortedContributionsInProfile() throws Exception {
        EasyMock.expect(store.getContributions()).andReturn(Collections.<Contribution>singleton(contribution));
        List<Contribution> list = Collections.singletonList(contribution);
        EasyMock.expect(dependencyService.orderForUninstall(EasyMock.isA(List.class))).andReturn(list);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        assertTrue(service.getSortedContributionsInProfile(profileUri).contains(contribution.getUri()));
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testRegisterProfiles() throws Exception {
        EasyMock.expect(store.getContributions()).andReturn(Collections.<Contribution>singleton(contribution));
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        URI newProfile = URI.create("newProfile");
        service.registerProfile(newProfile, Collections.<URI>singletonList(contributionUri));
        assertTrue(contribution.getProfiles().contains(newProfile));
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testInstallProfile() throws Exception {
        createResourceWithComposite();
        EasyMock.expect(store.getContributions()).andReturn(Collections.<Contribution>singleton(contribution));
        List<Contribution> list = Collections.singletonList(contribution);
        EasyMock.expect(dependencyService.order(EasyMock.isA(List.class))).andReturn(list);
        processorRegistry.processManifest(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));
        processorRegistry.indexContribution(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));
        processorRegistry.processContribution(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expect(loader.load(contribution)).andReturn(getClass().getClassLoader());
        store.store(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        service.installProfile(profileUri);
        assertTrue(ContributionState.INSTALLED == contribution.getState());
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testUnInstallProfile() throws Exception {
        contribution.setState(ContributionState.INSTALLED);
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.expect(store.getContributions()).andReturn(Collections.<Contribution>singleton(contribution));
        List<Contribution> list = Collections.singletonList(contribution);
        EasyMock.expect(dependencyService.orderForUninstall(EasyMock.isA(List.class))).andReturn(list);
        loader.unload(contribution);
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        service.uninstallProfile(profileUri);
        assertTrue(ContributionState.STORED == contribution.getState());
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    public void testRemoveProfile() throws Exception {
        contribution.setState(ContributionState.STORED);
        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        EasyMock.expect(store.getContributions()).andReturn(Collections.<Contribution>singleton(contribution));
        store.remove(contributionUri);
        Repository repository = EasyMock.createMock(Repository.class);
        repository.remove(contributionUri);
        service.setRepository(repository);

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService, repository);

        service.removeProfile(profileUri);
        assertTrue(ContributionState.STORED == contribution.getState());
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService, repository);
    }

    public void testErrorRemoveProfileInInstalledState() throws Exception {
        contribution.setState(ContributionState.INSTALLED);
        EasyMock.expect(store.getContributions()).andReturn(Collections.<Contribution>singleton(contribution));

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        try {
            service.removeProfile(profileUri);
            fail();
        } catch (RemoveException e) {
            // expected
        }
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testProcessManifest() throws Exception {
        createResourceWithComposite();
        URI otherContributionUri = URI.create("otherContribution");
        Contribution otherContribution = new Contribution(otherContributionUri);
        otherContribution.getManifest().addRequiredCapability(new Capability("capability", true));
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        sources.add(new FileContributionSource(contributionUri, null, -1, "application/xml", false));
        sources.add(new FileContributionSource(otherContributionUri, null, -1, "application/xml", false));
        List<Contribution> contributions = new ArrayList<Contribution>();
        contributions.add(contribution);
        contributions.add(otherContribution);

        EasyMock.expect(store.find(contributionUri)).andReturn(null);
        EasyMock.expect(store.find(otherContributionUri)).andReturn(null);
        store.store(contribution);
        store.store(otherContribution);
        processorRegistry.processManifest(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));
        processorRegistry.processManifest(EasyMock.eq(otherContribution), EasyMock.isA(IntrospectionContext.class));
        EasyMock.expect(dependencyService.order(EasyMock.isA(List.class))).andReturn(contributions);
        
        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);


        ContributionOrder order = service.processManifests(sources);
        assertTrue(order.getBaseContributions().contains(contributionUri));
        assertTrue(order.getIsolatedContributions().contains(otherContributionUri));
        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @SuppressWarnings({"unchecked"})
    public void testProcessContents() throws Exception {
        createResourceWithComposite();

        EasyMock.expect(store.find(contributionUri)).andReturn(contribution);
        store.store(contribution);
        EasyMock.expect(loader.load(contribution)).andReturn(getClass().getClassLoader());
        processorRegistry.indexContribution(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));
        processorRegistry.processContribution(EasyMock.eq(contribution), EasyMock.isA(IntrospectionContext.class));

        EasyMock.replay(processorRegistry, store, loader, resolver, dependencyService);

        service.processContents(contributionUri);

        EasyMock.verify(processorRegistry, store, loader, resolver, dependencyService);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        processorRegistry = EasyMock.createMock(ProcessorRegistry.class);
        store = EasyMock.createMock(MetaDataStore.class);
        loader = EasyMock.createMock(ContributionLoader.class);
        resolver = EasyMock.createMock(ContentTypeResolver.class);
        dependencyService = EasyMock.createMock(DependencyService.class);

        ContributionServiceMonitor monitor = EasyMock.createNiceMock(ContributionServiceMonitor.class);
        EasyMock.replay(monitor);

        contributionUri = URI.create("contribution");
        URL locationUrl = new URL("file://test");
        contribution = new Contribution(contributionUri, null, locationUrl, 1, "application/xml", false);
        deployableName = new QName("test", "composite");
        deployable = new Deployable(deployableName);
        contribution.getManifest().addDeployable(deployable);
        profileUri = URI.create("profile");
        contribution.addProfile(profileUri);
        service = new ContributionServiceImpl(processorRegistry, store, loader, resolver, dependencyService, monitor);
    }

    private void createResourceWithComposite() {
        Resource resource = new Resource(contribution, null, "application/xml");
        QNameSymbol symbol = new QNameSymbol(deployableName);
        Composite composite = new Composite(deployableName);
        ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol, composite);
        resource.addResourceElement(element);
        contribution.addResource(resource);
    }

}
