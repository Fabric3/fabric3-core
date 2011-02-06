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
package org.fabric3.management.rest.runtime;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.fabric3.management.rest.model.Link;
import org.fabric3.management.rest.model.Resource;
import org.fabric3.management.rest.model.SelfLink;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Collects and reports sub-resource information for a managed artifact.
 *
 * @version $Rev: 9923 $ $Date: 2011-02-03 17:11:06 +0100 (Thu, 03 Feb 2011) $
 */
public class ResourceInvoker {
    List<ManagedArtifactMapping> mappings;

    public ResourceInvoker(List<ManagedArtifactMapping> mappings) {
        this.mappings = mappings;
    }

    public Resource invoke(HttpServletRequest request) throws IOException {
        WorkContext workContext = new WorkContext();
        WorkContext old = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            URL url = new URL(request.getRequestURL().toString());
            SelfLink selfLink = new SelfLink(url);
            Resource resource = new Resource(selfLink);
            List<Link> links = new ArrayList<Link>();
            for (ManagedArtifactMapping mapping : mappings) {
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
            throw new IOException(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(old);
        }
    }

    private Object invoke(ManagedArtifactMapping mapping) {
        try {
            Object instance = mapping.getInstance();
            if (instance instanceof ObjectFactory) {
                instance = ((ObjectFactory) instance).getInstance();
            }
            return mapping.getMethod().invoke(instance);
        } catch (IllegalAccessException e) {
            // TODO return error
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            // TODO return error
            throw new AssertionError(e);
        } catch (ObjectCreationException e) {
            // TODO return error
            throw new AssertionError(e);
        }
    }

}
