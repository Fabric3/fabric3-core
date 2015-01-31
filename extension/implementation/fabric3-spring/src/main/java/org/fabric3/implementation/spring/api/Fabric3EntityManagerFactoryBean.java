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
 */
package org.fabric3.implementation.spring.api;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import java.util.Collections;

import org.fabric3.jpa.api.EntityManagerFactoryResolver;
import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.api.host.ContainerException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import static org.fabric3.implementation.spring.api.SpringConstants.EMF_RESOLVER;

/**
 * Integrates Fabric3 EntityManagerFactory parsing with Spring. This class can be configured in an end-user Spring application context to make entity
 * manager factories created by Fabric3 available to Spring.
 * <p/>
 * An example configuration is as follows:
 * <pre>
 *          &lt;bean id="EntityManagerFactory" class="org.fabric3.implementation.spring.api.Fabric3EntityManagerFactoryBean"&gt;
 *              &lt;property name="persistenceUnitName" value="loanApplication"/&gt;
 *          &lt;/bean&gt;
 * </pre>
 * The persistence unit name must be specified. Note that the datasource will be introspected from the <code>persistence.xml</code> file and should
 * not be set as a bean property.
 */
public class Fabric3EntityManagerFactoryBean extends LocalContainerEntityManagerFactoryBean {
    private static final long serialVersionUID = 3488984443450640577L;

    private transient EntityManagerFactoryResolver resolver;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        // Resolve the service responsible for building and caching EntityManagerFactory instances.
        // This is set in the parent application context owned by the Spring SCA component
        this.resolver = (EntityManagerFactoryResolver) beanFactory.getBean(EMF_RESOLVER);
    }

    protected EntityManagerFactory createNativeEntityManagerFactory() throws PersistenceException {
        if (resolver != null) {
            try {
                // note persistence overrides are not supported for Spring
                String unitName = getPersistenceUnitName();
                PersistenceOverrides overrides = new PersistenceOverrides(unitName, Collections.<String, String>emptyMap());
                return resolver.resolve(unitName, overrides, getBeanClassLoader());
            } catch (ContainerException e) {
                throw new PersistenceException(e);
            }
        }
        return super.createNativeEntityManagerFactory();
    }
}
