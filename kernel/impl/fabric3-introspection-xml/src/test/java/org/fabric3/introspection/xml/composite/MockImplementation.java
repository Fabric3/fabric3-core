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
package org.fabric3.introspection.xml.composite;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 *
 */
public class MockImplementation extends Implementation<InjectingComponentType> {
    private static final long serialVersionUID = 4898222089241004315L;
    public static QName TYPE = new QName(org.fabric3.api.Namespaces.F3, "implementation.testing");

    public String getType() {
        return "testing";
    }
}
