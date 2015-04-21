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
package org.fabric3.management.rest.spi;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Set;

import org.fabric3.api.Role;
import org.fabric3.management.rest.transformer.TransformerPair;

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
    private TransformerPair pair;
    private Set<Role> roles;

    /**
     * Constructor.
     *
     * @param identifier   the identifier used to group a set of mappings during deployment and undeployment
     * @param path         the resource path of the managed artifact relative to the base management URL
     * @param relativePath the resource path of the managed artifact relative to the containing resource. If the managed artifact is a top-level resource, the
     *                     path will be relative to the base management URL.
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
        this.identifier = identifier;
        this.path = path;
        this.relativePath = relativePath;
        this.verb = verb;
        this.method = method;
        this.instance = instance;
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
     * Returns the path relative to its containing resource.  If the managed artifact is a top-level resource, the path will be relative to the base management
     * URL.
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
