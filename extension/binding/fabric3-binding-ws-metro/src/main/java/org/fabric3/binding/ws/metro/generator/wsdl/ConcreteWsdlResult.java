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
package org.fabric3.binding.ws.metro.generator.wsdl;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

/**
 * Contains the generated result returned when synthesizing concreate WSDL information.
 */
public class ConcreteWsdlResult {
    private Definition definiton;
    private QName serviceName;
    private QName portName;

    public ConcreteWsdlResult(Definition definiton, QName serviceName, QName portName) {
        this.definiton = definiton;
        this.serviceName = serviceName;
        this.portName = portName;
    }

    /**
     * Returns the generated concrete WSDL.
     *
     * @return the concrete WSDL
     */
    public Definition getDefiniton() {
        return definiton;
    }

    /**
     * The name of the generated service
     *
     * @return the name of the generated service
     */
    public QName getServiceName() {
        return serviceName;
    }

    /**
     * The name of the generated port
     *
     * @return the name of the generated port
     */
    public QName getPortName() {
        return portName;
    }
}
