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
package org.fabric3.api.model.type.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.api.model.type.ModelObject;

/**
 * Encapsulates management metadata about a component implementation.
 */
public class ManagementInfo extends ModelObject<InjectingComponentType> {
    private static final long serialVersionUID = 8421549578785177167L;

    private String name;
    private String path;
    private String group;
    private String description;
    private String managementClass;
    private Set<Role> readRoles;
    private Set<Role> writeRoles;

    private List<ManagementOperationInfo> operations = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param name        the management name of this implementation
     * @param group       the management group of this implementation
     * @param path        the resource path
     * @param description the management description of this implementation
     * @param clazz       the management class
     * @param readRoles   roles allowed access to getter attributes
     * @param writeRoles  roles allowed access to setter attributes and operations
     */
    public ManagementInfo(String name, String group, String path, String description, String clazz, Set<Role> readRoles, Set<Role> writeRoles) {
        this.name = name;
        this.path = path;
        this.description = description;
        this.group = group;
        this.managementClass = clazz;
        this.readRoles = readRoles;
        this.writeRoles = writeRoles;
    }

    /**
     * Returns the management name of this implementation.
     *
     * @return the management name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the management group of this implementation.
     *
     * @return the management group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Returns the management resource path.
     *
     * @return the management resource path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the management description of this implementation.
     *
     * @return the management description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the management class.
     *
     * @return the management class
     */
    public String getManagementClass() {
        return managementClass;
    }

    /**
     * Returns the ordered collection of operations to be exposed to the runtime management framework.
     *
     * @return the ordered collection of operations to be exposed to the runtime management framework.
     */
    public List<ManagementOperationInfo> getOperations() {
        return operations;
    }

    /**
     * Adds an operation to be exposed to the runtime management framework.
     *
     * @param info the operation information
     */
    public void addOperation(ManagementOperationInfo info) {
        info.setParent(this);
        operations.add(info);
    }

    /**
     * Returns the roles allowed to access getter methods.
     *
     * @return the roles allowed to access getter methods.
     */
    public Set<Role> getReadRoles() {
        return readRoles;
    }

    /**
     * Returns the roles allowed to access setter methods and operations.
     *
     * @return the roles allowed to access setter methods and operations.
     */
    public Set<Role> getWriteRoles() {
        return writeRoles;
    }

}