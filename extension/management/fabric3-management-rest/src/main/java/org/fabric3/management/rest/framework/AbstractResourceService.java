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
package org.fabric3.management.rest.framework;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.management.rest.spi.ResourceListener;
import org.fabric3.management.rest.spi.ResourceMapping;

/**
 * Base functionality for a resource service that may be composed of extensible sub-resources.
 */
@Management
public abstract class AbstractResourceService implements ResourceListener {
    private List<ResourceMapping> subresources = new ArrayList<>();


    public void onRootResourceExport(ResourceMapping mapping) {
        if (mapping.getInstance() == this) {
            // don't track requests for this instance
            return;
        }
        String path = getResourcePath() + "/";
        if (!mapping.getPath().startsWith(path)) {
            // resource is not under specified path, return
            return;
        } else if (!mapping.isParameterized() && mapping.getPath().substring(path.length()).contains("/")) {
            return;
        }
        mapping = convertMapping(mapping);
        subresources.remove(mapping);
        subresources.add(mapping);
    }

    public void onSubResourceExport(ResourceMapping mapping) {
        // no-op
    }

    public void onRootResourceRemove(String identifier) {
        // no-op
    }

    public void onSubResourceRemove(String identifier) {
        // no-op
    }

    @ManagementOperation(path = "/")
    public Resource getResource(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        populateResource(resource, request);

        String requestUrl = ResourceHelper.getRequestUrl(request);
        for (ResourceMapping mapping : subresources) {
            String path;
            if ("/".equals(mapping.getRelativePath())) {
                // root resource, use the path setting
                path = mapping.getPath().substring(getResourcePath().length() + 1);
            } else {
                path = mapping.getRelativePath().substring(getResourcePath().length() + 1);
            }
            URL url = ResourceHelper.createUrl(requestUrl + '/' + path);
            Link link = new Link(path, Link.EDIT_LINK, url);
            resource.setProperty(link.getName(), link);
        }
        return resource;
    }

    /**
     * Returns the root path for this resource.
     *
     * @return the root path for this resource
     */
    protected abstract String getResourcePath();

    /**
     * Override to populate the resource with additional sub-resources.
     *
     * @param resource the resource to populate
     * @param request  the current HTTP request
     */
    protected void populateResource(Resource resource, HttpServletRequest request) {

    }

    /**
     * Override to convert the resource mapping.
     *
     * @param mapping the resource mapping
     * @return the converted resource mapping
     */
    protected ResourceMapping convertMapping(ResourceMapping mapping) {
        return mapping;
    }

    protected List<ResourceMapping> getSubresources() {
        return subresources;
    }
}
