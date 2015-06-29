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
package org.fabric3.spi.security;

import java.util.Collection;
import java.util.List;

import org.fabric3.api.SecuritySubject;

/**
 * Implemented by security providers that perform authorization.
 */
public interface AuthorizationService {

    /**
     * Determines if the subject has a role.
     *
     * @param subject the subject
     * @param role    the role
     * @throws AuthorizationException if the user does not have the role or there is a general error performing authorization. If the user does not have the
     *                                role, NotAuthorizedException will be thrown.
     */
    void checkRole(SecuritySubject subject, String role) throws AuthorizationException;

    /**
     * Determines if the subject has the collection of roles.
     *
     * @param subject the subject
     * @param roles   the roles
     * @throws AuthorizationException if the user does not have the roles or there is a general error performing authorization. If the user does not have a
     *                                role, NotAuthorizedException will be thrown.
     */
    void checkRoles(SecuritySubject subject, Collection<String> roles) throws AuthorizationException;

    /**
     * Determines if the subject has one of the roles in the collection of roles.
     *
     * @param subject the subject
     * @param roles   the roles
     * @throws AuthorizationException if the user does not have a roles or there is a general error performing authorization. If the user does not have at least
     *                                one role, NotAuthorizedException will be thrown.
     */
    void checkHasRole(SecuritySubject subject, List<String> roles);

    /**
     * Determines if the subject has a permission.
     *
     * @param subject the subject
     * @param role    the role
     * @throws AuthorizationException if the user does not have the permission or there is a general error performing authorization. If the user does not have
     *                                the permission, NotAuthorizedException will be thrown.
     */
    void checkPermission(SecuritySubject subject, String role) throws AuthorizationException;

    /**
     * Determines if the subject has the collection of permission.
     *
     * @param subject the subject
     * @param roles   the roles
     * @throws AuthorizationException if the user does not have the permissions or there is a general error performing authorization. If the user does not have
     *                                a permission, NotAuthorizedException will be thrown.
     */
    void checkPermissions(SecuritySubject subject, Collection<String> roles) throws AuthorizationException;

}
