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
package org.fabric3.spi.model.type.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.api.model.type.java.Signature;

/**
 * Represents a service contract specified using a Java interface
 */
public class JavaServiceContract extends ServiceContract {
    private Class<?> interfaceClass;
    private List<String> interfaces;
    private String superType;
    private List<Signature> methodSignatures;

    /**
     * Constructor used for testing.
     */
    public JavaServiceContract() {
    }

    /**
     * Constructor.
     *
     * @param interfaceClass the class representing the service contract
     */
    public JavaServiceContract(Class<?> interfaceClass) {
        introspectInterface(interfaceClass);
    }

    public String getQualifiedInterfaceName() {
        return getInterfaceClass().getName();
    }

    /**
     * Returns the class used to represent the service contract.
     *
     * @return the class name used to represent the service contract
     */
    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public List<String> getInterfaces() {
        return interfaces;
    }

    public String getSuperType() {
        return superType;
    }

    public List<Signature> getMethodSignatures() {
        return methodSignatures;
    }

    private void introspectInterface(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        methodSignatures = new ArrayList<>();
        Class<?> superClass = interfaceClass.getSuperclass();
        if (superClass != null) {
            superType = superClass.getName();
        }
        interfaces = new ArrayList<>();
        for (Method method : interfaceClass.getDeclaredMethods()) {
            Signature signature = new Signature(method);
            if (!methodSignatures.contains(signature)) {
                methodSignatures.add(signature);
            }
        }
        addInterfaces(interfaceClass, interfaces);
    }

    /**
     * Adds all interfaces implemented/extended by the class, including those of its ancestors.
     *
     * @param interfaze  the class to introspect
     * @param interfaces the collection of interfaces to add to
     */
    private void addInterfaces(Class<?> interfaze, List<String> interfaces) {
        for (Class<?> superInterface : interfaze.getInterfaces()) {
            if (!interfaces.contains(superInterface.getName())) {
                interfaces.add(superInterface.getName());
                addInterfaces(superInterface, interfaces);
            }
        }
    }

}
