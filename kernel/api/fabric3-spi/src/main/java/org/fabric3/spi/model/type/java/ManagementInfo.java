/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.model.type.ModelObject;

/**
 * Encapsulates management metadata about a component implementation.
 *
 * @version $Rev$ $Date$
 */
public class ManagementInfo extends ModelObject {
    private static final long serialVersionUID = 8421549578785177167L;

    private String name;
    private String path;
    private String group;
    private String description;
    private String managementClass;
    private Set<Role> readRoles;
    private Set<Role> writeRoles;

    private List<ManagementOperationInfo> operations = new ArrayList<ManagementOperationInfo>();

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