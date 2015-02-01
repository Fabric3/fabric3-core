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
package org.fabric3.contribution.processor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.spi.contribution.Constants.EXPLODED_CONTENT_TYPE;

/**
 * Processes a symbolic link contribution (*.contribution file). This is done by de-referencing the target directory specified in the .contribution file and
 * introspecting it. The introspection results are then copied to the <code>Contribution</code> representing the symbolic link.
 */
@EagerInit
public class SymLinkContributionProcessor implements ContributionProcessor {
    public static final String F3_SYMLINK = "f3.symlink";
    private ProcessorRegistry processorRegistry;

    public SymLinkContributionProcessor(@Reference ProcessorRegistry processorRegistry) {
        this.processorRegistry = processorRegistry;
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    @Destroy
    public void destroy() {
        processorRegistry.unregister(this);
    }

    public boolean canProcess(Contribution contribution) {
        String sourceUrl = contribution.getLocation().toString();
        return sourceUrl.endsWith(".contribution") || contribution.getMetaData(Boolean.class, F3_SYMLINK) != null;  // source url will change
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) {
        try {
            Contribution syntheticContribution = createSyntheticContribution(contribution);
            processorRegistry.processManifest(syntheticContribution, context);

            // override the locations
            contribution.setLocation(syntheticContribution.getLocation());
            contribution.getAdditionalLocations().addAll(syntheticContribution.getAdditionalLocations());

            contribution.setManifest(syntheticContribution.getManifest());
            contribution.addMetaData(F3_SYMLINK, Boolean.TRUE);
            contribution.addMetaData(contribution.getUri(), syntheticContribution);
        } catch (IOException e) {
            throw new Fabric3Exception(e);
        }
    }

    public void index(Contribution contribution, IntrospectionContext context) {
        Contribution syntheticContribution = contribution.getMetaData(Contribution.class, contribution.getUri());
        processorRegistry.indexContribution(syntheticContribution, context);
        for (Resource resource : syntheticContribution.getResources()) {
            resource.setContribution(contribution);
            contribution.addResource(resource);
        }
    }

    public void process(Contribution contribution, IntrospectionContext context) {
        Contribution syntheticContribution = contribution.getMetaData(Contribution.class, contribution.getUri());
        processorRegistry.processContribution(syntheticContribution, context);

        contribution.removeMetaData(contribution.getUri());
    }

    private Contribution createSyntheticContribution(Contribution contribution) throws IOException, Fabric3Exception {
        try {

            URL location = contribution.getLocation();
            Path symFile = Paths.get(location.toURI());
            List<String> paths = Files.readAllLines(symFile, Charset.defaultCharset());

            if (paths.isEmpty()) {
                throw new Fabric3Exception("Invalid contribution file: " + location);
            }

            // take the first entry in the file as the main contribution location
            File file = new File(paths.get(0));
            URL dereferencedLocation = file.toURI().toURL();
            URI contributionUri = URI.create(file.getName());

            Source source = new UrlSource(dereferencedLocation);
            long timestamp = System.currentTimeMillis();

            Contribution syntheticContribution = new Contribution(contributionUri, source, dereferencedLocation, timestamp, EXPLODED_CONTENT_TYPE);

            if (paths.size() > 1) {
                for (int i = 1; i < paths.size(); i++) {
                    String path = paths.get(i);
                    syntheticContribution.addAdditionalLocation(new File(path).toURI().toURL());
                }
            }

            return syntheticContribution;

        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}
