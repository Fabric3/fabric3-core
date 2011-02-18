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
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
@Management
public abstract class AbstractResourceService implements ResourceListener {
    private List<ResourceMapping> subresources = new ArrayList<ResourceMapping>();


    public void onRootResourceExport(ResourceMapping mapping) {
        if (mapping.getInstance() == this) {
            // don't track requests for this instance
            return;
        }
        if (!mapping.getPath().startsWith(getResourcePath() + "/")) {
            // resource is not under specified path, return
            return;
        }
        mapping = convertMapping(mapping);
        subresources.add(mapping);
    }

    @ManagementOperation(path = "/")
    public Resource getResource(HttpServletRequest request) {
        SelfLink selfLink = ResourceHelper.createSelfLink(request);
        Resource resource = new Resource(selfLink);

        populateResource(resource, request);

        String requestUrl = ResourceHelper.getRequestUrl(request);
        for (ResourceMapping mapping : subresources) {
            String path = mapping.getRelativePath().substring(getResourcePath().length() + 1); // +1 to remove leading '/' for relative link
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

}
