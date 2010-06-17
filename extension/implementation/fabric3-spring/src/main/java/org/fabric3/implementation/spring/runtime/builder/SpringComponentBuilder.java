/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.implementation.spring.runtime.builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.springframework.transaction.jta.JtaTransactionManager;

import org.fabric3.datasource.spi.DataSourceRegistry;
import org.fabric3.host.Names;
import static org.fabric3.implementation.spring.api.SpringConstants.EMF_RESOLVER;
import org.fabric3.implementation.spring.provision.SpringComponentDefinition;
import org.fabric3.implementation.spring.runtime.component.SCAApplicationContext;
import org.fabric3.implementation.spring.runtime.component.SpringComponent;
import org.fabric3.jpa.api.EmfResolver;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.cm.ComponentManager;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Builds a {@link SpringComponent} from a physical definition. Each SpringComponent contains an application context hierarchy.
 * <p/>
 * The parent context contains object factories for creating wire proxies for references configured on the component. In addition, the parent context
 * also contains system components configured to be aliased as Spring beans. By default, if a JTA transaction manager and datasources are configured
 * on the runtime, they will be aliased as <code>transactionManager</code> and their datasource name respectively. Other system components may be
 * aliased by configuring the <code>beanAliases</code> property.
 * <p/>
 * The child context contains beans defined in the configuration file specified by the location attribute of the Spring component.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SpringComponentBuilder implements ComponentBuilder<SpringComponentDefinition, SpringComponent> {
    private static final String TRX_ALIAS = "transactionManager";

    private ClassLoaderRegistry classLoaderRegistry;
    private ComponentManager componentManager;
    private boolean alias = true;
    private TransactionManager tm;
    private DataSourceRegistry dataSourceRegistry;
    private EmfResolver emfResolver;

    private Map<String, String> beanAliases = new HashMap<String, String>();

    @Property(required = false)
    public void setBeanAliases(Map<String, String> beanAliases) {
        this.beanAliases = beanAliases;
    }

    @Property(required = false)
    public void setAlias(boolean alias) {
        this.alias = alias;
    }

    @Reference(required = false)
    public void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }

    @Reference(required = false)
    public void setTm(TransactionManager tm) {
        this.tm = tm;
    }

    @Reference(required = false)
    public void setEmfBuilder(EmfResolver emfResolver) {
        this.emfResolver = emfResolver;
    }

    public SpringComponentBuilder(@Reference ClassLoaderRegistry classLoaderRegistry, @Reference ComponentManager componentManager) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.componentManager = componentManager;
    }

    public SpringComponent build(SpringComponentDefinition definition) throws BuilderException {
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());
        if (classLoader instanceof MultiParentClassLoader) {
            // add the extension classloader as a parent of the app classloader since Spring classes must be visible to the application
            // TODO add a filtering classloader to only expose specific Spring packages
            MultiParentClassLoader cl = (MultiParentClassLoader) classLoader;
            ClassLoader springClassLoader = getClass().getClassLoader();
            if (!cl.getParents().contains(springClassLoader)) {
                cl.addParent(springClassLoader);
            }
        }
        URL source = classLoader.getResource(definition.getLocation());
        URI componentUri = definition.getComponentUri();
        QName deployable = definition.getDeployable();
        SCAApplicationContext parent = createParentContext(classLoader);
        return new SpringComponent(componentUri, deployable, parent, source, classLoader);
    }

    /**
     * Creates a parent application context populated with system components configured to be aliased as Spring beans.
     *
     * @param classLoader the context classloader
     * @return the parent application context
     * @throws BuilderException if there is an error creating the context
     */
    private SCAApplicationContext createParentContext(ClassLoader classLoader) throws BuilderException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            SCAApplicationContext parent = new SCAApplicationContext();
            if (beanAliases.isEmpty() && alias && tm != null) {
                registerTransactionInfrastructure(parent);
            } else {
                registerAliases(parent);
            }
            return parent;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private void registerTransactionInfrastructure(SCAApplicationContext parent) {
        JtaTransactionManager platformTm = new JtaTransactionManager(tm);
        parent.getBeanFactory().registerSingleton(TRX_ALIAS, platformTm);
        for (Map.Entry<String, DataSource> entry : dataSourceRegistry.getDataSources().entrySet()) {
            parent.getBeanFactory().registerSingleton(entry.getKey(), entry.getValue());
        }
        if (emfResolver != null) {
            parent.getBeanFactory().registerSingleton(EMF_RESOLVER, emfResolver);
        }
    }

    private void registerAliases(SCAApplicationContext parent) throws ComponentNotFoundException, ComponentAliasException {
        for (Map.Entry<String, String> entry : beanAliases.entrySet()) {
            try {
                URI uri = new URI(Names.RUNTIME_NAME + "/" + entry.getKey());
                // note cast is safe as all system components are atomic
                AtomicComponent component = (AtomicComponent) componentManager.getComponent(uri);
                if (component == null) {
                    throw new ComponentNotFoundException("Component not found: " + entry.getKey());
                }
                WorkContext context = new WorkContext();
                InstanceWrapper wrapper = component.createInstanceWrapper(context);
                Object instance = wrapper.getInstance();
                parent.getBeanFactory().registerSingleton(entry.getKey(), instance);
            } catch (URISyntaxException e) {
                throw new ComponentAliasException("Illegal component name", e);
            } catch (ObjectCreationException e) {
                throw new ComponentAliasException("Unable to return instance", e);
            }
        }
    }
}