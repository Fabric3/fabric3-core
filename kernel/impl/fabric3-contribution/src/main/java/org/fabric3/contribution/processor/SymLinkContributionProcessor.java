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
package org.fabric3.contribution.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Processes a symbolic link contribution (*.contribution file). This is done by de-referencing the target directory specified in the .contribution
 * file and introspecting it. The introspection results are then copied to the <code>Contribution</code> representing the symbolic link.
 */
@EagerInit
public class SymLinkContributionProcessor implements ContributionProcessor {
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
        return sourceUrl.endsWith(".contribution");
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        try {
            Contribution syntheticContribution = createSyntheticContribution(contribution);
            processorRegistry.processManifest(syntheticContribution, context);
            // override the location
            contribution.setLocation(syntheticContribution.getLocation());
            contribution.setManifest(syntheticContribution.getManifest());
            contribution.addMetaData(contribution.getUri(), syntheticContribution);
        } catch (IOException e) {
            throw new InstallException(e);
        }
    }

    public void index(Contribution contribution, IntrospectionContext context) throws InstallException {
        Contribution syntheticContribution = contribution.getMetaData(Contribution.class, contribution.getUri());
        processorRegistry.indexContribution(syntheticContribution, context);
        for (Resource resource : syntheticContribution.getResources()) {
            resource.setContribution(contribution);
            contribution.addResource(resource);
        }
    }

    public void process(Contribution contribution, IntrospectionContext context) throws InstallException {
        Contribution syntheticContribution = contribution.getMetaData(Contribution.class, contribution.getUri());
        processorRegistry.processContribution(syntheticContribution, context);

        contribution.removeMetaData(contribution.getUri());
    }

    private Contribution createSyntheticContribution(Contribution contribution) throws IOException {
        InputStreamReader streamReader = new InputStreamReader(contribution.getLocation().openStream());
        BufferedReader bufferedReader = new BufferedReader(streamReader);
        String line = bufferedReader.readLine().trim();
        File file = new File(line);
        URL url = file.toURI().toURL();
        URI contributionUri = URI.create(file.getName());
        Source source = new UrlSource(url);
        long timestamp = System.currentTimeMillis();
        return new Contribution(contributionUri, source, url, timestamp, Constants.EXPLODED_CONTENT_TYPE, false);
    }

}
