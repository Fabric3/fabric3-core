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
package org.fabric3.spi.model.type.system;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.Implementation;
import org.fabric3.api.model.type.java.InjectingComponentType;

/**
 * Represents the system composite implementation
 */
public class SystemImplementation extends Implementation<InjectingComponentType> {
    private static final long serialVersionUID = -3698947089871597184L;
    public static final QName IMPLEMENTATION_SYSTEM = new QName(org.fabric3.api.Namespaces.F3, "implementation.system");
    private String implementationClass;

    public SystemImplementation() {
    }

    public QName getType() {
        return IMPLEMENTATION_SYSTEM;
    }

    public SystemImplementation(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public String getArtifactName() {
        return getImplementationClass();
    }

}
