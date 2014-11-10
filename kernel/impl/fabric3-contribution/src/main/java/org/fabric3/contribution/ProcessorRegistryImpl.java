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
package org.fabric3.contribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.contribution.UnsupportedContentTypeException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
public class ProcessorRegistryImpl implements ProcessorRegistry {
    private List<ContributionProcessor> contributionProcessorCache = new ArrayList<>();
    private Map<String, ResourceProcessor> resourceProcessorCache = new ConcurrentHashMap<>();

    public ProcessorRegistryImpl() {
    }

    public void register(ContributionProcessor processor) {
        contributionProcessorCache.add(processor);
    }

    public void unregister(ContributionProcessor processor) {
        contributionProcessorCache.remove(processor);
    }

    public void register(ResourceProcessor processor) {
        resourceProcessorCache.put(processor.getContentType(), processor);
    }

    public void unregister(String contentType) {
        resourceProcessorCache.remove(contentType);
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        ContributionProcessor processor = getContributionProcessor(contribution);
        processor.processManifest(contribution, context);
    }

    public void indexContribution(Contribution contribution, IntrospectionContext context) throws InstallException {
        ContributionProcessor processor = getContributionProcessor(contribution);
        processor.index(contribution, context);
    }

    public void indexResource(Resource resource, IntrospectionContext context) throws InstallException {
        String contentType = resource.getContentType();
        ResourceProcessor processor = resourceProcessorCache.get(contentType);
        if (processor == null) {
            // unknown type, skip
            return;
        }
        processor.index(resource, context);
    }

    public void processContribution(Contribution contribution, IntrospectionContext context) throws InstallException {
        ContributionProcessor processor = getContributionProcessor(contribution);
        processor.process(contribution, context);
    }

    public void processResource(Resource resource, IntrospectionContext context) throws InstallException {
        if (ResourceState.ERROR == resource.getState()) {
            // skip processing as the resource is in the error state
            return;
        }
        ResourceProcessor processor = resourceProcessorCache.get(resource.getContentType());
        if (processor == null) {
            return;
        }
        processor.process(resource, context);
    }

    public ContributionProcessor getContributionProcessor(Contribution contribution) throws UnsupportedContentTypeException {
        for (ContributionProcessor processor : contributionProcessorCache) {
            if (processor.canProcess(contribution)) {
                return processor;
            }
        }
        String source = contribution.getUri().toString();
        throw new UnsupportedContentTypeException("Processor not found for contribution " + source);
    }

}
