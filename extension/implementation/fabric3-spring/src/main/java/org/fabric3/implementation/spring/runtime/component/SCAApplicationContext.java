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
package org.fabric3.implementation.spring.runtime.component;

import org.fabric3.spi.container.objectfactory.ObjectFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Set as the parent of the application context associated with a Spring component. This parent will resolve wire and event stream proxies.
 */
public class SCAApplicationContext extends AbstractApplicationContext {
    private ProxyBeanFactory beanFactory = new ProxyBeanFactory();

    public void add(String name, Class<?> type, ObjectFactory factory) {
        beanFactory.add(name, type, factory);
    }

    public ObjectFactory remove(String name) {
        return beanFactory.remove(name);
    }


    protected void refreshBeanFactory() throws BeansException, IllegalStateException {

    }

    protected void closeBeanFactory() {

    }

    public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
        return beanFactory;
    }
}
