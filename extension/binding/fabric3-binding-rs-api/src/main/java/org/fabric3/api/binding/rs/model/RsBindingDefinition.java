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
package org.fabric3.api.binding.rs.model;

import javax.xml.namespace.QName;
import java.net.URI;

import org.fabric3.api.model.type.component.BindingDefinition;

/**
 * Configures a service to be exposed as a JAX-RS resource.
 */
public class RsBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = 7344870455465600359L;

    public static final QName BINDING_RS = new QName(org.fabric3.api.Namespaces.F3, "binding.rs");

    public RsBindingDefinition(String name, URI serviceUri) {
        super(name, serviceUri, BINDING_RS);
    }
}
