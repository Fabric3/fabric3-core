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
package org.fabric3.api.model.type.java;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.Implementation;
import org.oasisopen.sca.Constants;

/**
 * Represents a Java component implementation type.
 */
public class JavaImplementation extends Implementation<InjectingComponentType> {
    public static final QName IMPLEMENTATION_JAVA = new QName(Constants.SCA_NS, "implementation.java");
    private static final long serialVersionUID = 8922589166061811190L;
    private String implementationClass;
    private transient Object instance;

    public JavaImplementation() {
    }

    public JavaImplementation(Object instance) {
        this.instance = instance;
        this.implementationClass = instance.getClass().getName();
    }

    public QName getType() {
        return IMPLEMENTATION_JAVA;
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

    public Object getInstance() {
        return instance;
    }
}
