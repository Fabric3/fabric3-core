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
package org.fabric3.management.rest.runtime;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.fabric3.api.Role;
import org.fabric3.api.SecuritySubject;
import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;

/**
 * Collects and reports sub-resource information for a managed artifact.
 */
public class ResourceInvoker {
    List<ResourceMapping> mappings;
    boolean securityCheck;

    public ResourceInvoker(List<ResourceMapping> mappings, ManagementSecurity security) {
        this.mappings = mappings;
        if (security == ManagementSecurity.DISABLED) {
            securityCheck = false;
        } else {
            for (ResourceMapping mapping : mappings) {
                if (!mapping.getRoles().isEmpty()) {
                    securityCheck = true;
                    break;
                }
            }
        }
    }

    /**
     * Invokes all GET operations managed artifact and returns values as a single (root) resource representation. The merged values will be included a
     * properties where the property name is the relative path of the sub-resource. In addition to the merged values, the representation will contain a links
     * property to sub-resources, keyed by sub-resource name (relative path). For example:
     *
     * <pre>
     * {"selfLink":{...},
     * "count":10,
     * "links":[{"name":"count","rel":"edit","href":"...."}]
     * }
     *
     *
     * <pre>
     *
     * @param request the HTTP request
     * @return the resource representation
     * @throws Fabric3Exception  if there is an error processing the request
     * @throws ResourceException if the client is not authorized to invoke an operation
     */
    public Resource invoke(HttpServletRequest request) throws Fabric3Exception {
        try {
            WorkContext workContext = WorkContextCache.getThreadWorkContext();
            if (workContext == null) {
                throw new AssertionError("Work context not set");
            }
            checkSecurity(workContext);
            URL url = new URL(request.getRequestURL().toString());
            SelfLink selfLink = new SelfLink(url);
            Resource resource = new Resource(selfLink);
            List<Link> links = new ArrayList<>();
            // invoke the sub-resources and merge the responses into the root resource representation
            for (ResourceMapping mapping : mappings) {
                Object object = invoke(mapping);
                String relativePath = mapping.getRelativePath();
                resource.setProperty(relativePath, object);
                URL linkUrl = new URL(request.getRequestURL().append("/").append(relativePath).toString());
                Link link = new Link(relativePath, Link.EDIT_LINK, linkUrl);
                links.add(link);
            }
            resource.setProperty("links", links);
            return resource;
        } catch (MalformedURLException e) {
            throw new Fabric3Exception(e);
        }
    }

    private void checkSecurity(WorkContext workContext) throws ResourceException {
        if (securityCheck) {
            SecuritySubject subject = workContext.getSubject();
            if (subject == null) {
                throw new ResourceException(HttpStatus.UNAUTHORIZED, "Unauthorized");
            }
            for (ResourceMapping mapping : mappings) {
                boolean found = false;
                for (Role role : mapping.getRoles()) {
                    if (subject.getRoles().contains(role)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new ResourceException(HttpStatus.UNAUTHORIZED, "Unauthorized");
                }
            }
        }
    }

    private Object invoke(ResourceMapping mapping) throws Fabric3Exception {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Object instance = mapping.getInstance();
            if (instance instanceof Supplier) {
                instance = ((Supplier) instance).get();
            }
            Thread.currentThread().setContextClassLoader(instance.getClass().getClassLoader());
            return mapping.getMethod().invoke(instance);
        } catch (IllegalArgumentException | Fabric3Exception | InvocationTargetException | IllegalAccessException e) {
            throw new Fabric3Exception("Error invoking operation: " + mapping.getMethod(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}
