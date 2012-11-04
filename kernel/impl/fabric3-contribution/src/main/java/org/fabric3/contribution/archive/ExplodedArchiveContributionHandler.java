/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.host.util.FileHelper;
import org.fabric3.spi.contribution.ContentTypeResolutionException;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.archive.Action;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;

import static org.fabric3.spi.contribution.Constants.EXPLODED_CONTENT_TYPE;

/**
 * Handles exploded archives on a filesystem.
 */
public class ExplodedArchiveContributionHandler implements ArchiveContributionHandler {
    private Loader loader;
    private final ContentTypeResolver contentTypeResolver;

    public ExplodedArchiveContributionHandler(@Reference Loader loader, @Reference ContentTypeResolver contentTypeResolver) {
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
    }

    public boolean canProcess(Contribution contribution) {
        URL location = contribution.getLocation();
        if (location == null || !"file".equals(location.getProtocol())) {
            return false;
        }
        File file = new File(location.getFile());
        String contentType = contribution.getContentType();
        return file.isDirectory()
                && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip") || EXPLODED_CONTENT_TYPE.equals(contentType));
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        ContributionManifest manifest;
        try {
            String sourceUrl = contribution.getLocation().toString();

            URL manifestUrl = new URL(sourceUrl + "/META-INF/sca-contribution.xml");
            File file = new File(manifestUrl.getPath());
            if (!file.exists()) {
                manifestUrl = new URL(sourceUrl + "/WEB-INF/sca-contribution.xml");
            }
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

    public void iterateArtifacts(Contribution contribution, Action action) throws InstallException {
        File root = FileHelper.toFile(contribution.getLocation());
        iterateArtifactsRecursive(contribution, action, root, root);
    }

    protected void iterateArtifactsRecursive(Contribution contribution, Action action, File dir, File root) throws InstallException {
        File[] files = dir.listFiles();
        ContributionManifest manifest = contribution.getManifest();
        for (File file : files) {
            if (file.isDirectory()) {
                iterateArtifactsRecursive(contribution, action, file, root);
            } else {
                try {
                    if (file.getName().equals("sca-contribution.xml")) {
                        // don't index the manifest
                        continue;
                    }
                    URL entryUrl = file.toURI().toURL();
                    String contentType = contentTypeResolver.getContentType(entryUrl);
                    // skip entry if we don't recognize the content type
                    if (contentType == null) {
                        continue;
                    }
                    if (exclude(manifest, file, root)) {
                        continue;
                    }
                    action.process(contribution, contentType, entryUrl);
                } catch (MalformedURLException e) {
                    throw new InstallException(e);
                } catch (IOException e) {
                    throw new InstallException(e);
                } catch (ContentTypeResolutionException e) {
                    throw new InstallException(e);
                }
            }
        }

    }

    private boolean exclude(ContributionManifest manifest, File file, File root) {
        for (Pattern pattern : manifest.getScanExcludes()) {
            // construct a file name relative to the root directory as excludes are relative to the archive root  
            String relativePathName = file.toURI().toString().substring(root.toURI().toString().length());
            if (pattern.matcher(relativePathName).matches()) {
                return true;
            }
        }
        return false;
    }


}
