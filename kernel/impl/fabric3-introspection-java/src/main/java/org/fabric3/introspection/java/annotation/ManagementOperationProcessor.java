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
package org.fabric3.introspection.java.annotation;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.ManagementInfo;
import org.fabric3.api.model.type.java.ManagementOperationInfo;
import org.fabric3.api.model.type.java.OperationType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;

/**
 * Processes the {@link ManagementOperation} annotation on a component implementation class.
 */
public class ManagementOperationProcessor extends AbstractAnnotationProcessor<ManagementOperation> {

    public ManagementOperationProcessor() {
        super(ManagementOperation.class);
    }

    public void visitMethod(ManagementOperation annotation,
                            Method method,
                            Class<?> implClass,
                            InjectingComponentType componentType,
                            IntrospectionContext context) {
        ManagementInfo info = componentType.getManagementInfo();
        if (info == null) {
            // there was no management annotation on the type - record an error
            Class<?> clazz = method.getDeclaringClass();
            String name = Management.class.getSimpleName();
            context.addError(new InvalidAnnotation("Implementation is missing @" + name, method, annotation, clazz));
            return;
        }
        String description = annotation.description();
        if (description.trim().length() == 0) {
            description = null;
        }
        Set<Role> roles = new HashSet<>();
        for (String roleName : annotation.rolesAllowed()) {
            roles.add(new Role(roleName));
        }
        String path = annotation.path();

        org.fabric3.api.annotation.management.OperationType operationType = annotation.type();
        OperationType type = OperationType.valueOf(operationType.toString());

        ManagementOperationInfo operationInfo = new ManagementOperationInfo(method, path, type, description, roles);
        info.addOperation(operationInfo);
    }

}