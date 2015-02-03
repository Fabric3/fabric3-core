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
package org.fabric3.binding.ws.metro.generator.java.codegen;

import org.fabric3.api.host.Fabric3Exception;

/**
 * Generates an interface with JAX-WS annotations from another interface. This allows classes with non-annotated interfaces to be used with Metro, which
 * requires interfaces that define service endpoints to be annotated. Specifically, adds @WebService to the generated interface, @WebMethod to all methods, and
 * @Oneway to methods marked with the SCA @OneWay annotation.
 */
public interface InterfaceGenerator {

    /**
     * Determines if a service interface or class needs to be enhanced with JAX-WS annotations. Enhancement via bytecode generation will need to be done if:
     * <pre>
     * - The class does not contain a <code>WebService</code> annotation
     * - The class contains a method marked with the <code>org.oasisopen.sca.annotation.OneWay</code> annotation
     * </pre>
     *
     * @param clazz the class to check
     * @return true if the class needs to be enhanced
     */
    public boolean doGeneration(Class<?> clazz);

    /**
     * Generates the annotated interface from another interface
     *
     * @param interfaze       the source interface
     * @param targetNamespace the target namespace to use with the @WebService annotation or null.
     * @param wsdlLocation    the WSDL location to use with the @WebService annotation or null.
     * @param serviceName     the service name to use with the @WebService annotation or null.
     * @param portName        the port name to use with the @WebService annotation or null.
     * @return the generated interface result
     * @throws Fabric3Exception if an error generating the exception occurs
     */
    GeneratedInterface generate(Class interfaze, String targetNamespace, String wsdlLocation, String serviceName, String portName) throws Fabric3Exception;

}