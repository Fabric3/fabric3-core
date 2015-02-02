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
package org.fabric3.implementation.web.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.java.InjectionSite;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.implementation.web.provision.WebContextInjectionSite;
import org.fabric3.spi.container.injection.InjectionAttributes;
import org.fabric3.spi.container.injection.Injector;
import org.fabric3.spi.model.type.java.FieldInjectionSite;
import org.fabric3.spi.model.type.java.MethodInjectionSite;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of the InjectorFactory.
 */
public class InjectorFactoryImpl implements InjectorFactory {
    private ReflectionFactory reflectionFactory;

    public InjectorFactoryImpl(@Reference ReflectionFactory reflectionFactory) {
        this.reflectionFactory = reflectionFactory;
    }

    public void createInjectorMappings(Map<String, List<Injector<?>>> injectors,
                                       Map<String, Map<String, InjectionSite>> siteMappings,
                                       Map<String, Supplier<?>> suppliers,
                                       ClassLoader classLoader) {
        for (Map.Entry<String, Supplier<?>> entry : suppliers.entrySet()) {
            String siteName = entry.getKey();
            Supplier<?> supplier = entry.getValue();
            Map<String, InjectionSite> artifactMapping = siteMappings.get(siteName);
            if (artifactMapping == null) {
                throw new Fabric3Exception("Injection site not found for: " + siteName);
            }
            for (Map.Entry<String, InjectionSite> siteEntry : artifactMapping.entrySet()) {
                String artifactName = siteEntry.getKey();
                InjectionSite site = siteEntry.getValue();
                List<Injector<?>> injectorList = injectors.get(artifactName);
                if (injectorList == null) {
                    injectorList = new ArrayList<>();
                    injectors.put(artifactName, injectorList);
                }
                Injector<?> injector;
                if (site instanceof WebContextInjectionSite) {
                    injector = createInjector(siteName, supplier, (WebContextInjectionSite) site);
                } else if (site instanceof FieldInjectionSite) {
                    injector = reflectionFactory.createInjector(((FieldInjectionSite) site).getField(), supplier);
                } else if (site instanceof MethodInjectionSite) {
                    injector = reflectionFactory.createInjector(((MethodInjectionSite) site).getMethod(), supplier);
                } else {
                    throw new UnsupportedOperationException("Unsupported injection site type: " + site.getClass());
                }
                injectorList.add(injector);
            }
        }
    }

    private Injector<?> createInjector(String referenceName, Supplier<?> supplier, WebContextInjectionSite site) {
        // use reference name as the key
        InjectionAttributes attributes = new InjectionAttributes(referenceName, Integer.MIN_VALUE);
        if (site.getContextType() == WebContextInjectionSite.ContextType.SERVLET_CONTEXT) {
            Injector<?> injector = new ServletContextInjector();
            injector.setSupplier(supplier, attributes);
            return injector;
        } else {
            Injector<?> injector = new HttpSessionInjector();
            injector.setSupplier(supplier, attributes);
            return injector;
        }
    }

}
