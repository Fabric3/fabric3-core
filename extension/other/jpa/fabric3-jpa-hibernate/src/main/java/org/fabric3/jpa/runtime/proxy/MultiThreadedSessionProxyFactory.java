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
package org.fabric3.jpa.runtime.proxy;

import javax.transaction.TransactionManager;

import org.fabric3.api.host.ContainerException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

/**
 * Creates MultiThreadedSessionProxy instances.
 */
public class MultiThreadedSessionProxyFactory implements ObjectFactory<MultiThreadedSessionProxy> {
    private String unitName;
    private EntityManagerService service;
    private TransactionManager tm;

    public MultiThreadedSessionProxyFactory(String unitName, EntityManagerService service, TransactionManager tm) {
        this.service = service;
        this.tm = tm;
        this.unitName = unitName;
    }

    public MultiThreadedSessionProxy getInstance() throws ContainerException {
        return new MultiThreadedSessionProxy(unitName, service, tm);
    }
}