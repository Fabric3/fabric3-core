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
package org.fabric3.node.nonmanaged;

import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Models non-managed code that is deployed as a component to a service fabric.
 */
public class NonManagedImplementation extends Implementation<InjectingComponentType> {
    private static final long serialVersionUID = 8179450453871659967L;

    public String getType() {
        return "NonManaged";
    }
}
