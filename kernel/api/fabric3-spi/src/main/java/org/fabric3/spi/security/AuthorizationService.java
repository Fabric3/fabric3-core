/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
*/
package org.fabric3.spi.security;

import java.util.Collection;

import org.fabric3.api.SecuritySubject;

/**
 * Implemented by security providers that perform authorization.
 *
 * @version $Rev$ $Date$
 */
public interface AuthorizationService {

    /**
     * Determines if the subject has a role.
     *
     * @param subject the subject
     * @param role    the role
     * @throws AuthorizationException if the user does not have the role or there is a general error performing authorization. If the user does not
     *                                have the role, NotAuthorizedException will be thrown.
     */
    void checkRole(SecuritySubject subject, String role) throws AuthorizationException;

    /**
     * Determines if the subject has the collection of roles.
     *
     * @param subject the subject
     * @param roles   the roles
     * @throws AuthorizationException if the user does not have the roles or there is a general error performing authorization. If the user does not
     *                                have a role, NotAuthorizedException will be thrown.
     */
    void checkRoles(SecuritySubject subject, Collection<String> roles) throws AuthorizationException;

    /**
     * Determines if the subject has a permission.
     *
     * @param subject the subject
     * @param role    the role
     * @throws AuthorizationException if the user does not have the permission or there is a general error performing authorization. If the user does
     *                                not have the permission, NotAuthorizedException will be thrown.
     */
    void checkPermission(SecuritySubject subject, String role) throws AuthorizationException;

    /**
     * Determines if the subject has the collection of permission.
     *
     * @param subject the subject
     * @param roles   the roles
     * @throws AuthorizationException if the user does not have the permissions or there is a general error performing authorization. If the user does
     *                                not have a permission, NotAuthorizedException will be thrown.
     */
    void checkPermissions(SecuritySubject subject, Collection<String> roles) throws AuthorizationException;

}
