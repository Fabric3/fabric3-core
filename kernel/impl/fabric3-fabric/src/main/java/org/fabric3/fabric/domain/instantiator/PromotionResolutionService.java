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
package org.fabric3.fabric.domain.instantiator;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Resolves promoted services and references by setting the resolved promotion URI of the logical component service or reference that is being
 * promoted.
 */
public interface PromotionResolutionService {

    /**
     * Resolve the promoted service and references for the logical component and its children.
     *
     * @param component the logical component
     * @param context   the instantiation context
     */
    public void resolve(LogicalComponent<?> component, InstantiationContext context);

}
