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
package org.fabric3.spi.model.type.binding;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.BindingDefinition;

/**
 * Represents binding information for the source or target side of a bound wire. For bound services, the remote binding definition is used to
 * represent the side of the wire that is attached to to the component providing the service. For bound references, the remote binding definition is
 * used to represent the side of the wire that is attached to the component containing the reference.
 */
public class RemoteBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = 975283470994901368L;
    private static final QName QNAME = new QName(org.fabric3.api.Namespaces.F3, "binding.remote");

    public static final RemoteBindingDefinition INSTANCE = new RemoteBindingDefinition();

    public RemoteBindingDefinition() {
        super(null, QNAME);
    }
}
