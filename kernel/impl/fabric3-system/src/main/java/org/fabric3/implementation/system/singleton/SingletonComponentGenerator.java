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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.system.singleton;

import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.model.type.java.Injectable;
import org.fabric3.api.model.type.java.InjectableType;
import org.fabric3.spi.domain.generator.component.ComponentGenerator;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalConsumer;
import org.fabric3.spi.model.instance.LogicalProducer;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponent;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.model.physical.PhysicalConnectionTarget;
import org.fabric3.spi.model.physical.PhysicalWireSource;
import org.fabric3.spi.model.physical.PhysicalWireTarget;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class SingletonComponentGenerator implements ComponentGenerator<LogicalComponent<SingletonImplementation>> {

    public PhysicalComponent generate(LogicalComponent<SingletonImplementation> component) throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSource generateSource(LogicalReference reference) throws Fabric3Exception {
        SingletonWireSource source = new SingletonWireSource();
        URI uri = reference.getUri();
        source.setOptimizable(true);
        source.setUri(uri);
        source.setInjectable(new Injectable(InjectableType.REFERENCE, uri.getFragment()));

        return source;
    }

    public PhysicalWireTarget generateTarget(LogicalService service) throws Fabric3Exception {
        SingletonWireTarget target = new SingletonWireTarget();
        URI uri = service.getUri();
        target.setUri(uri);
        target.setOptimizable(true);
        return target;
    }

    public PhysicalWireSource generateResourceSource(LogicalResourceReference<?> resourceReference) throws Fabric3Exception {
        SingletonWireSource source = new SingletonWireSource();
        URI uri = resourceReference.getUri();
        source.setOptimizable(true);
        source.setUri(uri);
        source.setInjectable(new Injectable(InjectableType.RESOURCE, uri.getFragment()));
        return source;
    }

    public PhysicalConnectionSource generateConnectionSource(LogicalProducer producer) {
        throw new UnsupportedOperationException();
    }

    public PhysicalConnectionTarget generateConnectionTarget(LogicalConsumer consumer) throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireSource generateCallbackSource(LogicalService service) throws Fabric3Exception {
        throw new UnsupportedOperationException();
    }

}
