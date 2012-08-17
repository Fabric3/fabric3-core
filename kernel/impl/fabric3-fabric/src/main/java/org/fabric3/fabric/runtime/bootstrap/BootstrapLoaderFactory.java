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
package org.fabric3.fabric.runtime.bootstrap;

import org.fabric3.implementation.system.introspection.SystemImplementationLoader;
import org.fabric3.implementation.system.model.SystemImplementation;
import org.fabric3.introspection.xml.DefaultLoaderHelper;
import org.fabric3.introspection.xml.LoaderRegistryImpl;
import org.fabric3.introspection.xml.common.ComponentConsumerLoader;
import org.fabric3.introspection.xml.common.ComponentProducerLoader;
import org.fabric3.introspection.xml.common.ComponentReferenceLoader;
import org.fabric3.introspection.xml.common.ComponentServiceLoader;
import org.fabric3.introspection.xml.common.PropertyLoader;
import org.fabric3.introspection.xml.composite.ComponentLoader;
import org.fabric3.introspection.xml.composite.CompositeLoader;
import org.fabric3.introspection.xml.composite.IncludeLoader;
import org.fabric3.introspection.xml.composite.PropertyValueLoader;
import org.fabric3.introspection.xml.composite.WireLoader;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.introspection.xml.CompositeConstants;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Creates a Loader that processes bootstrap system composite, which uses a constrained version of the SCA programming model. The constraints are:
 * <pre>
 * <ul>
 * <li>The only implementation type allowed is system</li>
 * <li>The only service contract type is a Java interface found through introspection</li>
 * <li>Resolution of SCDL artifacts by QName is not supported; scdlResource must be used</li>
 * </ul>
 * </pre>
 *
 * @version $Rev$ $Date$
 */
public class BootstrapLoaderFactory {

    private BootstrapLoaderFactory() {
    }

    public static Loader createLoader(ImplementationProcessor processor, XMLFactory xmlFactory) {
        LoaderHelper loaderHelper = new DefaultLoaderHelper();

        LoaderRegistryImpl registry = new LoaderRegistryImpl(xmlFactory);

        // loader for <implementation.system> elements
        SystemImplementationLoader systemLoader = new SystemImplementationLoader(processor);
        registry.registerLoader(SystemImplementation.IMPLEMENTATION_SYSTEM, systemLoader);

        // loader for <wire> elements
        WireLoader wireLoader = new WireLoader(loaderHelper);
        registry.registerLoader(CompositeConstants.WIRE, wireLoader);

        // loader for <composite> documents
        compositeLoader(registry, loaderHelper);

        return registry;
    }

    private static CompositeLoader compositeLoader(LoaderRegistry registry, LoaderHelper loaderHelper) {
        PropertyValueLoader propertyValueLoader = new PropertyValueLoader(registry, loaderHelper);
        propertyValueLoader.init();
        ComponentReferenceLoader componentReferenceLoader = new ComponentReferenceLoader(registry, loaderHelper);
        componentReferenceLoader.init();
        ComponentServiceLoader componentServiceLoader = new ComponentServiceLoader(registry, loaderHelper);
        componentServiceLoader.init();
        ComponentConsumerLoader componentConsumerLoader = new ComponentConsumerLoader(registry);
        componentConsumerLoader.init();
        ComponentProducerLoader componentProducerLoader = new ComponentProducerLoader(registry);
        componentProducerLoader.init();

        ComponentLoader componentLoader = new ComponentLoader(registry, loaderHelper);
        componentLoader.init();
        IncludeLoader includeLoader = new IncludeLoader(registry);
        includeLoader.init();
        PropertyLoader propertyLoader = new PropertyLoader(loaderHelper);
        CompositeLoader compositeLoader = new CompositeLoader(registry, propertyLoader, loaderHelper);
        compositeLoader.init();
        return compositeLoader;
    }


}
