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
package org.fabric3.contribution.archive;

import java.net.URL;
import java.util.List;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.contribution.UnsupportedContentTypeException;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Handles common processing for contribution archives
 */
public class ArchiveContributionProcessor extends AbstractContributionProcessor {
    private List<ArchiveContributionHandler> handlers;

    @Reference
    public void setHandlers(List<ArchiveContributionHandler> handlers) {
        this.handlers = handlers;
    }

    public boolean canProcess(Contribution contribution) {
        for (ArchiveContributionHandler handler : handlers) {
            if (handler.canProcess(contribution)) {
                return true;
            }
        }
        return false;
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        ArchiveContributionHandler handler = getHandler(contribution);
        handler.processManifest(contribution, context);
    }

    public void index(Contribution contribution, final IntrospectionContext context) throws InstallException {
        ArchiveContributionHandler handler = getHandler(contribution);
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = context.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            handler.iterateArtifacts(contribution, new Action() {
                public void process(Contribution contribution, String contentType, URL url) throws InstallException {
                    UrlSource source = new UrlSource(url);
                    registry.indexResource(contribution, contentType, source, context);
                }
            });
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }

    }

    public void process(Contribution contribution, IntrospectionContext context) throws InstallException {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = context.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            for (Resource resource : contribution.getResources()) {
                if (ResourceState.UNPROCESSED == resource.getState()) {
                    registry.processResource(resource, context);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    private ArchiveContributionHandler getHandler(Contribution contribution) throws UnsupportedContentTypeException {
        for (ArchiveContributionHandler handler : handlers) {
            if (handler.canProcess(contribution)) {
                return handler;
            }
        }
        String source = contribution.getUri().toString();
        throw new UnsupportedContentTypeException("Contribution type not supported: " + source);
    }

}
