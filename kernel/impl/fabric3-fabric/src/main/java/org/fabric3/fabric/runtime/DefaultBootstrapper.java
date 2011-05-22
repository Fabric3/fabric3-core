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
package org.fabric3.fabric.runtime;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;

import org.w3c.dom.Document;

import org.fabric3.contribution.manifest.ContributionExport;
import org.fabric3.fabric.instantiator.component.AtomicComponentInstantiatorImpl;
import org.fabric3.fabric.runtime.bootstrap.BootExports;
import org.fabric3.fabric.runtime.bootstrap.BootstrapAssemblyFactory;
import org.fabric3.fabric.runtime.bootstrap.BootstrapCompositeFactory;
import org.fabric3.fabric.runtime.bootstrap.BootstrapIntrospectionFactory;
import org.fabric3.fabric.runtime.bootstrap.Java6HostExports;
import org.fabric3.fabric.synthesizer.SingletonComponentSynthesizer;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.repository.Repository;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.ComponentRegistration;
import org.fabric3.host.runtime.Fabric3Runtime;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.implementation.system.model.SystemImplementation;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.introspection.java.contract.JavaContractProcessorImpl;
import org.fabric3.model.type.component.ChannelDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.channel.ChannelManager;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.contribution.Capability;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionState;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Version;
import org.fabric3.spi.contribution.manifest.JavaExport;
import org.fabric3.spi.contribution.manifest.PackageInfo;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.lcm.LogicalComponentManager;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalProperty;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.synthesize.ComponentRegistrationException;
import org.fabric3.spi.synthesize.ComponentSynthesizer;

import static org.fabric3.host.Names.BOOT_CONTRIBUTION;
import static org.fabric3.host.Names.HOST_CONTRIBUTION;
import static org.fabric3.host.Names.RUNTIME_MONITOR_CHANNEL;
import static org.fabric3.host.Names.RUNTIME_MONITOR_CHANNEL_URI;

/**
 * The default Bootstrapper implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultBootstrapper implements Bootstrapper {
    private static final URI RUNTIME_SERVICES = URI.create("fabric3://RuntimeServices");

    // bootstrap components - these are disposed of after the core runtime system components are booted
    private JavaContractProcessorImpl contractProcessor;
    private AtomicComponentInstantiatorImpl instantiator;
    private ImplementationProcessor<SystemImplementation> implementationProcessor;
    private ComponentSynthesizer synthesizer;

    // runtime components - these are persistent and supplied by the runtime implementation
    private MonitorProxyService monitorService;
    private ClassLoaderRegistry classLoaderRegistry;
    private MetaDataStore metaDataStore;
    private ScopeRegistry scopeRegistry;
    private LogicalCompositeComponent domain;
    private LogicalComponentManager lcm;
    private ComponentManager componentManager;
    private ChannelManager channelManager;
    private ScopeContainer scopeContainer;
    private Repository repository;
    private MBeanServer mbeanServer;
    private ManagementService managementService;
    private HostInfo hostInfo;

    private Domain runtimeDomain;

    private Fabric3Runtime runtime;
    private URL systemCompositeUrl;
    private Document systemConfig;
    private ClassLoader bootClassLoader;
    private Map<String, String> exportedPackages;
    private List<String> hostCapabilities;
    private ClassLoader hostClassLoader;
    private Contribution bootContribution;
    private List<ComponentRegistration> registrations;

    public DefaultBootstrapper(BootConfiguration configuration) {
        runtime = configuration.getRuntime();
        systemCompositeUrl = configuration.getSystemCompositeUrl();
        systemConfig = configuration.getSystemConfig();
        hostClassLoader = configuration.getHostClassLoader();
        bootClassLoader = configuration.getBootClassLoader();
        exportedPackages = configuration.getExportedPackages();
        hostCapabilities = configuration.getHostCapabilities();
        registrations = configuration.getRegistrations();

        // create disposable components needed to bootstrap the runtime
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        contractProcessor = new JavaContractProcessorImpl(helper);
        instantiator = new AtomicComponentInstantiatorImpl();
        implementationProcessor = BootstrapIntrospectionFactory.createSystemImplementationProcessor();
    }

    public void bootRuntimeDomain() throws InitializationException {
        RuntimeServices runtimeServices = runtime.getComponent(RuntimeServices.class, RUNTIME_SERVICES);
        hostInfo = runtimeServices.getHostInfo();
        monitorService = runtimeServices.getMonitorProxyService();
        lcm = runtimeServices.getLogicalComponentManager();
        componentManager = runtimeServices.getComponentManager();
        channelManager = runtimeServices.getChannelManager();
        domain = lcm.getRootComponent();
        classLoaderRegistry = runtimeServices.getClassLoaderRegistry();
        metaDataStore = runtimeServices.getMetaDataStore();
        scopeRegistry = runtimeServices.getScopeRegistry();
        scopeContainer = runtimeServices.getScopeContainer();
        repository = runtimeServices.getRepository();
        mbeanServer = runtimeServices.getMBeanServer();
        managementService = runtimeServices.getManagementService();
        hostInfo = runtimeServices.getHostInfo();

        synthesizer = new SingletonComponentSynthesizer(implementationProcessor,
                                                        instantiator,
                                                        lcm,
                                                        componentManager,
                                                        contractProcessor,
                                                        scopeContainer);

        // register components provided by the runtime itself so they may be wired to
        registerRuntimeComponents(registrations);

        runtimeDomain = BootstrapAssemblyFactory.createDomain(monitorService,
                                                              classLoaderRegistry,
                                                              scopeRegistry,
                                                              componentManager,
                                                              lcm,
                                                              channelManager,
                                                              metaDataStore,
                                                              managementService,
                                                              hostInfo);

        // register the runtime domain component
        registerComponent("RuntimeDomain", Domain.class, runtimeDomain, true);


        // register the domain channel
        registerRuntimeDomainChannel();

        // create host and boot contributions
        synthesizeContributions();
    }

    public void bootSystem() throws InitializationException {
        try {
            // load the system composite
            Composite composite =
                    BootstrapCompositeFactory.createSystemComposite(systemCompositeUrl, bootContribution, bootClassLoader, implementationProcessor);

            // create the property and merge it into the composite
            LogicalProperty logicalProperty = new LogicalProperty("systemConfig", systemConfig, false, domain);
            domain.setProperties(logicalProperty);

            // deploy the composite to the runtime domain
            runtimeDomain.include(composite);
        } catch (DeploymentException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Registers the primordial runtime components.
     *
     * @param registrations host components to register
     * @throws InitializationException if there is an error during registration
     */
    @SuppressWarnings({"unchecked"})
    private <S, I extends S> void registerRuntimeComponents(List<ComponentRegistration> registrations) throws InitializationException {

        // services available through the outward facing Fabric3Runtime API
        registerComponent("MonitorProxyService", MonitorProxyService.class, monitorService, true);
        Class<HostInfo> type = getHostInfoType(hostInfo);
        registerComponent("HostInfo", type, hostInfo, true);
        if (mbeanServer != null) {
            registerComponent("MBeanServer", MBeanServer.class, mbeanServer, false);
        }
        registerComponent("ManagementService", ManagementService.class, managementService, true);


        // services available through the inward facing RuntimeServices SPI
        registerComponent("ComponentManager", ComponentManager.class, componentManager, true);
        registerComponent("ChannelManager", ChannelManager.class, channelManager, true);
        registerComponent("RuntimeLogicalComponentManager", LogicalComponentManager.class, lcm, true);
        registerComponent("CompositeScopeContainer", ScopeContainer.class, scopeContainer, true);
        registerComponent("ClassLoaderRegistry", ClassLoaderRegistry.class, classLoaderRegistry, true);
        registerComponent("ScopeRegistry", ScopeRegistry.class, scopeRegistry, true);
        registerComponent("MetaDataStore", MetaDataStore.class, metaDataStore, true);
        registerComponent("Repository", Repository.class, repository, true);

        // register other components provided by the host environment
        for (ComponentRegistration registration : registrations) {
            String name = registration.getName();
            Class<S> service = (Class<S>) registration.getService();
            I instance = (I) registration.getInstance();
            boolean introspect = registration.isIntrospect();
            registerComponent(name, service, instance, introspect);
        }
    }

    /**
     * Determines the specific HostInfo interface subtype to register the HostInfo instance with.
     *
     * @param info the HostInfo
     * @return the interface to register the HostInfo instance with
     */
    @SuppressWarnings({"unchecked"})
    private Class<HostInfo> getHostInfoType(HostInfo info) {
        Class<?>[] interfaces = info.getClass().getInterfaces();
        if (interfaces.length == 1) {
            return HostInfo.class;
        }
        for (Class<?> interfaze : interfaces) {
            if (!HostInfo.class.equals(interfaze) && HostInfo.class.isAssignableFrom(interfaze)) {
                return (Class<HostInfo>) interfaze;
            }
        }
        return HostInfo.class;
    }

    /**
     * Registers a primordial component.
     *
     * @param name       the component name
     * @param type       the service interface type
     * @param instance   the component instance
     * @param introspect true if the component should be introspected for references
     * @throws InitializationException if there is an error during registration
     */
    private <S, I extends S> void registerComponent(String name, Class<S> type, I instance, boolean introspect) throws InitializationException {
        try {
            synthesizer.registerComponent(name, type, instance, introspect);
        } catch (ComponentRegistrationException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Registers the runtime domain channels.
     */
    private void registerRuntimeDomainChannel() {
        ChannelDefinition definition = new ChannelDefinition(RUNTIME_MONITOR_CHANNEL, BOOT_CONTRIBUTION);
        LogicalCompositeComponent domain = lcm.getRootComponent();
        LogicalChannel logicalChannel = new LogicalChannel(RUNTIME_MONITOR_CHANNEL_URI, definition, domain);
        logicalChannel.setState(LogicalState.PROVISIONED);
        domain.addChannel(logicalChannel);
    }

    /**
     * Creates contributions for the host and boot classloaders. These contributions may be imported by extensions and user contributions.
     *
     * @throws InitializationException if there is an error synthesizing the contributions
     */
    private void synthesizeContributions() throws InitializationException {
        try {
            // export packages included in JDK 6
            synthesizeContribution(HOST_CONTRIBUTION, Java6HostExports.getExports(), hostCapabilities, hostClassLoader);
            // add default boot exports
            exportedPackages.putAll(BootExports.getExports());
            bootContribution = synthesizeContribution(BOOT_CONTRIBUTION, exportedPackages, Collections.<String>emptyList(), bootClassLoader);
        } catch (ContributionException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Synthesizes a contribution from a classloader and installs it.
     *
     * @param contributionUri  the contribution URI
     * @param exportedPackages the packages exported by the contribution
     * @param hostCapabilities the capabilities provided by the contribution
     * @param loader           the classloader
     * @return the synthesized contribution
     * @throws ContributionException if there is an error synthesizing the contribution
     */
    private Contribution synthesizeContribution(URI contributionUri,
                                                Map<String, String> exportedPackages,
                                                List<String> hostCapabilities,
                                                ClassLoader loader) throws ContributionException {
        Contribution contribution = new Contribution(contributionUri);
        contribution.setState(ContributionState.INSTALLED);
        ContributionManifest manifest = contribution.getManifest();
        // add the ContributionExport
        manifest.addExport(new ContributionExport(contributionUri));
        for (Map.Entry<String, String> entry : exportedPackages.entrySet()) {
            Version version = new Version(entry.getValue());
            PackageInfo info = new PackageInfo(entry.getKey(), version);
            JavaExport export = new JavaExport(info);
            manifest.addExport(export);
        }
        for (String capability : hostCapabilities) {
            manifest.addProvidedCapability(new Capability(capability));
        }
        metaDataStore.store(contribution);
        classLoaderRegistry.register(contributionUri, loader);
        return contribution;
    }


}