/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.management.rest.runtime;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.fabric3.api.Role;
import org.fabric3.api.SecuritySubject;
import org.fabric3.management.rest.model.HttpStatus;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.ResourceException;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.management.rest.spi.ResourceMapping;
import org.fabric3.spi.container.invocation.WorkContext;
import org.fabric3.spi.container.invocation.WorkContextCache;
import org.fabric3.spi.container.objectfactory.ObjectCreationException;
import org.fabric3.spi.container.objectfactory.ObjectFactory;

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
     * properties where the property name is the relative path of the sub-resource. In addition to the merged values, the representation will contain
     * a links property to sub-resources, keyed by sub-resource name (relative path). For example:
     * <p/>
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
     * @throws ResourceProcessingException if there is an error processing the request
     * @throws ResourceException           if the client is not authorized to invoke an operation
     */
    public Resource invoke(HttpServletRequest request) throws ResourceProcessingException, ResourceException {
        try {
            WorkContext workContext = WorkContextCache.getThreadWorkContext();
            if (workContext == null) {
                throw new AssertionError("Work context not set");
            }
            checkSecurity(workContext);
            URL url = new URL(request.getRequestURL().toString());
            SelfLink selfLink = new SelfLink(url);
            Resource resource = new Resource(selfLink);
            List<Link> links = new ArrayList<Link>();
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
            throw new ResourceProcessingException(e);
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

    private Object invoke(ResourceMapping mapping) throws ResourceProcessingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Object instance = mapping.getInstance();
            if (instance instanceof ObjectFactory) {
                instance = ((ObjectFactory) instance).getInstance();
            }
            Thread.currentThread().setContextClassLoader(instance.getClass().getClassLoader());
            return mapping.getMethod().invoke(instance);
        } catch (IllegalArgumentException e) {
            throw new ResourceProcessingException("Error invoking operation: " + mapping.getMethod(), e);
        } catch (IllegalAccessException e) {
            throw new ResourceProcessingException("Error invoking operation: " + mapping.getMethod(), e);
        } catch (InvocationTargetException e) {
            throw new ResourceProcessingException("Error invoking operation: " + mapping.getMethod(), e);
        } catch (ObjectCreationException e) {
            throw new ResourceProcessingException("Error invoking operation: " + mapping.getMethod(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}
