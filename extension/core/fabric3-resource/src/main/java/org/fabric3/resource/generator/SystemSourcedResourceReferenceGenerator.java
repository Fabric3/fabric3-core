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
package org.fabric3.resource.generator;

import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.Names;
import org.fabric3.resource.model.SystemSourcedResourceReference;
import org.fabric3.resource.provision.SystemSourcedWireTarget;
import org.fabric3.spi.domain.generator.ResourceReferenceGenerator;
import org.fabric3.spi.model.instance.LogicalResourceReference;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class SystemSourcedResourceReferenceGenerator implements ResourceReferenceGenerator<SystemSourcedResourceReference> {

    public SystemSourcedWireTarget generateWireTarget(LogicalResourceReference<SystemSourcedResourceReference> resourceReference) throws Fabric3Exception {
        SystemSourcedResourceReference definition = resourceReference.getDefinition();
        String mappedName = definition.getMappedName();
        URI targetUri = URI.create(Names.RUNTIME_NAME + "/" + mappedName);
        SystemSourcedWireTarget target = new SystemSourcedWireTarget();
        target.setUri(targetUri);
        return target;
    }

}
