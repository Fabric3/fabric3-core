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

import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.api.model.type.ModelObject;

/**
 * Encapsulates management information about a component operation.
 */
public class ManagementOperationInfo extends ModelObject<ManagementInfo> {
    private static final long serialVersionUID = 138617917546848298L;

    private Signature signature;
    private String path;
    private String description;
    private Set<Role> roles;
    private OperationType type;

    public ManagementOperationInfo(Signature signature, String path, OperationType type, String description, Set<Role> roles) {
        this.signature = signature;
        this.path = path;
        this.type = type;
        this.description = description;
        this.roles = roles;
    }

    public Signature getSignature() {
        return signature;
    }

    public String getPath() {
        return path;
    }

    public OperationType getOperationType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}