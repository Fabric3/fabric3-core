/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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

    private static final long serialVersionUID = -7360275776965712638L;
    // NOTE: this class cannot reference the actual Java class it represents as contract comparison may be performed
    // across classloaders. This class may also be deserialized as part of a domain assembly in a context where the
    // Java class may not be present on the classpath.
    private String interfaceClass;
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
        return getInterfaceClass();
    }

    /**
     * Returns the fully qualified class name used to represent the service contract.
     *
     * @return the class name used to represent the service contract
     */
    public String getInterfaceClass() {
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
        this.interfaceClass = interfaceClass.getName();
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
