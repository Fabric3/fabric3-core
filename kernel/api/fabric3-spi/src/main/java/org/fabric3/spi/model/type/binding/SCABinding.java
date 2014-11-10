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
package org.fabric3.spi.model.type.binding;

import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.Target;

/**
 * Represents a service or reference explicitly bound using <code>binding.sca</code>.
 */
public class SCABinding extends BindingDefinition {
    private static final long serialVersionUID = 5329743408485507984L;
    private static final QName TYPE = new QName(Constants.SCA_NS, "binding.sca");
    private Target target;

    public SCABinding(String name, Target target) {
        super(name, null, TYPE);
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }
}