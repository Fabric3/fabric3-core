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

import java.util.HashSet;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.annotation.AbstractAnnotationProcessor;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.api.model.type.java.ManagementInfo;

/**
 * Processes the {@link Management} annotation on a component implementation class.
 */
public class ManagementProcessor extends AbstractAnnotationProcessor<Management> {

    public ManagementProcessor() {
        super(Management.class);
    }

    public void visitType(Management annotation, Class<?> type, InjectingComponentType componentType, IntrospectionContext context) {
        componentType.setManaged(true);
        String name = annotation.name();
        if (name.trim().length() == 0) {
            name = type.getSimpleName();
        }
        String group = annotation.group();
        if (group.trim().length() == 0) {
            group = null;
        }
        String description = annotation.description();
        if (description.trim().length() == 0) {
            description = null;
        }
        Set<Role> readRoles = new HashSet<>();
        for (String roleName : annotation.readRoles()) {
            readRoles.add(new Role(roleName));
        }
        Set<Role> writeRoles = new HashSet<>();
        for (String roleName : annotation.writeRoles()) {
            writeRoles.add(new Role(roleName));
        }
        String path = annotation.path();
        ManagementInfo info = new ManagementInfo(name, group, path, description, type.getName(), readRoles, writeRoles);
        ManagementInfo overriden = componentType.getManagementInfo();
        if (overriden != null) {
            // A management annotation was defined in a super class - override it, preserving management operations
            info.getOperations().addAll(overriden.getOperations());
        }
        componentType.setManagementInfo(info);
    }

}