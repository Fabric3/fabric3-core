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
package org.fabric3.binding.ws.metro.generator;

import javax.xml.namespace.QName;

/**
 * A parsed WSDL element expression.
 */
public class WsdlElement {
    public enum Type {
        PORT, SERVICE, BINDING
    }

    private QName serviceName;
    private QName portName;
    private QName bindingName;

    private Type type;

    public WsdlElement(QName serviceName, QName portName) {
        this.serviceName = serviceName;
        this.portName = portName;
        this.type = Type.PORT;
    }

    public WsdlElement(QName name, Type type) {
        if (Type.SERVICE == type) {
            this.serviceName = name;
        } else if (Type.BINDING == type) {
            this.bindingName = name;
        } else {
            throw new IllegalArgumentException("Must by a service or binding name");
        }
        this.type = type;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public QName getPortName() {
        return portName;
    }

    public QName getBindingName() {
        return bindingName;
    }

    public Type getType() {
        return type;
    }
}