/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.runtime.maven.contribution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.host.util.FileHelper;
import org.fabric3.spi.contribution.ContentTypeResolutionException;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;

/**
 * Processes a Maven module directory.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ModuleContributionProcessor implements ContributionProcessor {
    private static final String MAVEN_CONTENT_TYPE = "application/vnd.fabric3.maven-project";

    private ProcessorRegistry registry;
    private ContentTypeResolver contentTypeResolver;
    private Loader loader;

    public ModuleContributionProcessor(@Reference ProcessorRegistry registry,
                                       @Reference ContentTypeResolver contentTypeResolver,
                                       @Reference Loader loader) {
        this.registry = registry;
        this.contentTypeResolver = contentTypeResolver;
        this.loader = loader;
    }

    public boolean canProcess(Contribution contribution) {
        return MAVEN_CONTENT_TYPE.equals(contribution.getContentType());
    }

    @Init
    public void init() {
        registry.register(this);
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

    public void processManifest(Contribution contribution, final IntrospectionContext context) throws InstallException {
        ContributionManifest manifest;
        try {
            URL sourceUrl = contribution.getLocation();
            URL manifestUrl = new URL(sourceUrl.toExternalForm() + "/classes/META-INF/sca-contribution.xml");
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext childContext = new DefaultIntrospectionContext(uri, cl);
            Source source = new UrlSource(manifestUrl);
            manifest = loader.load(source, ContributionManifest.class, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }
            contribution.setManifest(manifest);
        } catch (LoaderException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                // ignore no manifest found
            } else {
                throw new InstallException(e);
            }
        } catch (MalformedURLException e) {
            // ignore no manifest found
        }

    }

    public void index(Contribution contribution, final IntrospectionContext context) throws InstallException {
        iterateArtifacts(contribution, context, new Action() {
            public void process(Contribution contribution, String contentType, URL url) throws InstallException {
                UrlSource source = new UrlSource(url);
                registry.indexResource(contribution, contentType, source, context);
            }
        });
    }

    private void iterateArtifacts(Contribution contribution, final IntrospectionContext context, Action action) throws InstallException {
        File root = FileHelper.toFile(contribution.getLocation());
        assert root.isDirectory();
        iterateArtifactsRecursive(contribution, context, action, root);
    }

    private void iterateArtifactsRecursive(Contribution contribution, final IntrospectionContext context, Action action, File dir)
            throws InstallException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                iterateArtifactsRecursive(contribution, context, action, file);
            } else {
                try {
                    URL entryUrl = file.toURI().toURL();
                    String contentType = contentTypeResolver.getContentType(entryUrl);
                    // skip entry if we don't recognize the content type
                    if (contentType == null) {
                        continue;
                    }
                    action.process(contribution, contentType, entryUrl);
                } catch (MalformedURLException e) {
                    context.addWarning(new ContributionIndexingFailure(file, e));
                } catch (IOException e) {
                    context.addWarning(new ContributionIndexingFailure(file, e));
                } catch (ContentTypeResolutionException e) {
                    context.addWarning(new ContributionIndexingFailure(file, e));
                }
            }
        }

    }
}