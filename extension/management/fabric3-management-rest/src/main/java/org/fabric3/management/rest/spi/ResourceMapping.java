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
package org.fabric3.management.rest.spi;

import java.lang.reflect.Method;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.fabric3.api.Role;
import org.fabric3.management.rest.runtime.TransformerPair;

/**
 * Maps an operation on a managed resource to a path relative to the management servlet and REST verb.
 */
public class ResourceMapping {
    private String identifier;
    private String path;
    private String relativePath;
    private Verb verb;
    private Method method;
    private Object instance;
    private boolean replicate;
    private TransformerPair pair;
    private Set<Role> roles;

    /**
     * Constructor.
     *
     * @param identifier   the identifier used to group a set of mappings during deployment and undeployment
     * @param path         the resource path of the managed artifact relative to the base management URL
     * @param relativePath the resource path of the managed artifact relative to the containing resource. If the managed artifact is a top-level
     *                     resource, the path will be relative to the base management URL.
     * @param verb         the HTTP verb the management operation is mapped to
     * @param method       the management operation
     * @param instance     the managed artifact
     * @param pair         the transformer pair used to (de)serialize request/response types
     * @param roles        the roles required to invoke the operation
     */
    public ResourceMapping(String identifier,
                           String path,
                           String relativePath,
                           Verb verb,
                           Method method,
                           Object instance,
                           TransformerPair pair,
                           Set<Role> roles) {
        this(identifier, path, relativePath, verb, method, instance, false, pair, roles);
    }

    /**
     * Constructor.
     *
     * @param identifier   the identifier used to group a set of mappings during deployment and undeployment
     * @param path         the resource path of the managed artifact relative to the base management URL
     * @param relativePath the resource path of the managed artifact relative to the containing resource. If the managed artifact is a top-level
     *                     resource, the path will be relative to the base management URL.
     * @param verb         the HTTP verb the management operation is mapped to
     * @param method       the management operation
     * @param instance     the managed artifact
     * @param replicate    true if invocations should be replicated in a cluster
     * @param pair         the transformer pair used to (de)serialize request/response types
     * @param roles        the roles required to invoke the operation
     */
    public ResourceMapping(String identifier,
                           String path,
                           String relativePath,
                           Verb verb,
                           Method method,
                           Object instance,
                           boolean replicate,
                           TransformerPair pair,
                           Set<Role> roles) {
        this.identifier = identifier;
        this.path = path;
        this.relativePath = relativePath;
        this.verb = verb;
        this.method = method;
        this.instance = instance;
        this.replicate = replicate;
        this.pair = pair;
        this.roles = roles;
    }

    /**
     * Returns the identifier used to group a set of mappings during deployment and undeployment.
     *
     * @return the identifier used to group a set of mappings during deployment and undeployment
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the path relative to the base management URL the operation should be mapped to.
     *
     * @return the path relative to the base management URL the operation should be mapped to
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the path relative to its containing resource.  If the managed artifact is a top-level resource, the path will be relative to the base
     * management URL.
     *
     * @return the path relative to its containing resource
     */
    public String getRelativePath() {
        return relativePath;
    }

    /**
     * Returns true if the path is parameterized. That is, if the resource operation takes part of the URL as a parameter.
     *
     * @return true if the path is parameterized
     */
    public boolean isParameterized() {
        if (Verb.PUT == verb || Verb.POST == verb) {
            if (method.getParameterTypes().length > 0 && (HttpServletRequest.class.isAssignableFrom(method.getParameterTypes()[0]))) {
                return true;
            }
        }
        return method.getParameterTypes().length > 0 && !(HttpServletRequest.class.isAssignableFrom(method.getParameterTypes()[0]));
    }

    /**
     * Returns the verb the operation is mapped to.
     *
     * @return the verb the operation is mapped to
     */
    public Verb getVerb() {
        return verb;
    }


    /**
     * Returns the operation.
     *
     * @return the operation
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the managed artifact instance.
     *
     * @return the managed artifact instance
     */
    public Object getInstance() {
        return instance;
    }

    /**
     * Returns true if invocations should be replicated in a cluster.
     *
     * @return true if invocations should be replicated in a cluster
     */
    public boolean isReplicate() {
        return replicate;
    }

    /**
     * Returns the pair for (de)serializing input and output parameters.
     *
     * @return the pair for (de)serializing input and output parameters
     */
    public TransformerPair getPair() {
        return pair;
    }

    /**
     * Returns the roles required to execute the management operation.
     *
     * @return the roles required to execute the management operation.
     */
    public Set<Role> getRoles() {
        return roles;
    }
}
