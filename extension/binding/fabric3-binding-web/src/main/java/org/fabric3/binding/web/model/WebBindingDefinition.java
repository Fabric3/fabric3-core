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
package org.fabric3.binding.web.model;

import javax.xml.namespace.QName;

import org.fabric3.binding.web.common.OperationsAllowed;
import org.fabric3.api.model.type.component.BindingDefinition;

/**
 *
 */
public class WebBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = 3182899822938972425L;
    private static final QName BINDING_WEB = new QName(org.fabric3.api.Namespaces.F3, "binding.web");

    private OperationsAllowed allowed;
    private String wireFormat;

    public WebBindingDefinition(String name, OperationsAllowed allowed, String wireFormat) {
        super(name, null, BINDING_WEB);
        this.allowed = allowed;
        this.wireFormat = wireFormat;
    }

    public OperationsAllowed getAllowed() {
        return allowed;
    }

    public String getWireFormat() {
        return wireFormat;
    }

}
