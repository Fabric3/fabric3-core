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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.contribution.archive;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.oasisopen.sca.annotation.Reference;

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

    public void processManifest(Contribution contribution, IntrospectionContext context) {
        ArchiveContributionHandler handler = getHandler(contribution);
        handler.processManifest(contribution, context);
    }

    public void index(Contribution contribution, final IntrospectionContext context) {
        ArchiveContributionHandler handler = getHandler(contribution);
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = context.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            handler.iterateArtifacts(contribution, resource -> {
                registry.indexResource(resource, context);
            }, context);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }

    }

    public void process(Contribution contribution, IntrospectionContext context) {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = context.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            List<Resource> copy = new ArrayList<>(contribution.getResources());   // copy the list since processors may add resources
            copy.stream().filter(resource -> ResourceState.UNPROCESSED == resource.getState()).forEach(resource -> {
                registry.processResource(resource, context);
            });
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    private ArchiveContributionHandler getHandler(Contribution contribution) {
        for (ArchiveContributionHandler handler : handlers) {
            if (handler.canProcess(contribution)) {
                return handler;
            }
        }
        throw new Fabric3Exception("Contribution type not supported: " + contribution.getUri());
    }

}
