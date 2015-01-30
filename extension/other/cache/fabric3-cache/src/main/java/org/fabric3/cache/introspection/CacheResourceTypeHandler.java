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

package org.fabric3.cache.introspection;

import java.lang.reflect.Member;
import java.util.concurrent.ConcurrentMap;

import org.fabric3.api.annotation.Resource;
import org.fabric3.api.model.type.component.ResourceReference;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.cache.model.CacheReference;
import org.fabric3.cache.spi.MissingCacheName;
import org.fabric3.resource.spi.ResourceTypeHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.JavaContractProcessor;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Introspects {@link Resource} annotations when used with <code>ConcurrentMap</code> types.
 */
@EagerInit
public class CacheResourceTypeHandler implements ResourceTypeHandler {
    private ServiceContract contract;
    private JavaContractProcessor contractProcessor;

    public CacheResourceTypeHandler(@Reference JavaContractProcessor contractProcessor) {
        this.contractProcessor = contractProcessor;
    }

    @Init
    public void init() {
        // introspect the interface once
        contract = contractProcessor.introspect(ConcurrentMap.class, new DefaultIntrospectionContext());
    }


    public ResourceReference createResourceReference(String name,
                                                               Resource annotation,
                                                               Member member,
                                                               InjectingComponentType componentType,
                                                               IntrospectionContext context) {
        String cacheName = annotation.name();
        if (cacheName.length() == 0) {
            MissingCacheName error = new MissingCacheName(member, componentType);
            context.addError(error);
            return new CacheReference(name, contract, false, "error");
        }
        return new CacheReference(name, contract, false, cacheName);
    }
}
