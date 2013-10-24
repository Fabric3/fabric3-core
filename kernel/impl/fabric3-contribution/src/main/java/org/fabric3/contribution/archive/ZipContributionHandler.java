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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Constants;
import org.fabric3.spi.contribution.ContentTypeResolutionException;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.ProviderSymbol;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.contribution.archive.ArtifactResourceCallback;
import org.fabric3.spi.contribution.manifest.JarManifestHandler;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Introspects a Zip-based contribution, delegating to ResourceProcessors for handling leaf-level children.
 */
@EagerInit
public class ZipContributionHandler implements ArchiveContributionHandler {
    private List<JarManifestHandler> manifestHandlers = Collections.emptyList();
    private final Loader loader;
    private final ContentTypeResolver contentTypeResolver;

    public ZipContributionHandler(@Reference Loader loader, @Reference ContentTypeResolver contentTypeResolver) {
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
    }

    @Reference(required = false)
    public void setManifestHandlers(List<JarManifestHandler> manifestHandlers) {
        this.manifestHandlers = manifestHandlers;
    }

    public boolean canProcess(Contribution contribution) {
        String sourceUrl = contribution.getLocation().toString();
        return sourceUrl.endsWith(".jar") || sourceUrl.endsWith(".zip");
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        URL sourceUrl = contribution.getLocation();
        try {
            URL manifestUrl = new URL("jar:" + sourceUrl.toExternalForm() + "!/META-INF/sca-contribution.xml");
            ClassLoader cl = getClass().getClassLoader();
            URI uri = contribution.getUri();
            IntrospectionContext childContext = new DefaultIntrospectionContext(uri, cl);
            Source source = new UrlSource(manifestUrl);
            ContributionManifest manifest = loader.load(source, ContributionManifest.class, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }
            if (childContext.hasErrors() || childContext.hasWarnings()) {
                return;
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
        InputStream manifestStream = null;
        try {
            URL jarUrl = new URL("jar:" + sourceUrl.toExternalForm() + "!/META-INF/MANIFEST.MF");
            manifestStream = jarUrl.openStream();
            Manifest jarManifest = new Manifest(manifestStream);
            for (JarManifestHandler handler : manifestHandlers) {
                handler.processManifest(contribution.getManifest(), jarManifest, context);
            }
        } catch (MalformedURLException e) {
            // ignore no manifest found
        } catch (FileNotFoundException e) {
            // ignore no manifest found
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (manifestStream != null) {
                    manifestStream.close();
                }
            } catch (IOException e) {
                // ignore
                e.printStackTrace();
            }
        }
    }

    public void iterateArtifacts(Contribution contribution, ArtifactResourceCallback callback) throws InstallException {
        URL location = contribution.getLocation();
        ContributionManifest manifest = contribution.getManifest();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(location.openStream());
            while (true) {
                ZipEntry entry = zipStream.getNextEntry();
                if (entry == null) {
                    // EOF
                    break;
                }
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();

                if (name.contains("META-INF/sca-contribution.xml")) {
                    // don't index the manifest
                    continue;
                }

                // check if it is a DSL provider
                if (isProvider(name)) {
                    URL entryUrl = new URL("jar:" + location.toExternalForm() + "!/" + name);
                    UrlSource source = new UrlSource(entryUrl);
                    Resource resource = new Resource(contribution, source, Constants.DSL_CONTENT_TYPE);
                    String className = "f3." + name.substring(3, name.length() - 6).replace("/", ".");
                    ProviderSymbol symbol = new ProviderSymbol(className);
                    ResourceElement<Symbol, Object> element = new ResourceElement<Symbol, Object>(symbol);
                    resource.addResourceElement(element);
                    contribution.addResource(resource);

                    callback.onResource(resource);
                } else {
                    if (exclude(manifest, entry)) {
                        continue;
                    }

                    String contentType = contentTypeResolver.getContentType(new URL(location, name));
                    if (contentType == null) {
                        // skip entry if we don't recognize the content type
                        continue;
                    }
                    URL entryUrl = new URL("jar:" + location.toExternalForm() + "!/" + name);
                    UrlSource source = new UrlSource(entryUrl);
                    Resource resource = new Resource(contribution, source, contentType);
                    contribution.addResource(resource);
                    callback.onResource(resource);
                }
            }
        } catch (ContentTypeResolutionException e) {
            throw new InstallException(e);
        } catch (MalformedURLException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (zipStream != null) {
                    zipStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isProvider(String name) {
        return name.startsWith("f3/") && name.endsWith("Provider.class");
    }

    private boolean exclude(ContributionManifest manifest, ZipEntry entry) {
        for (Pattern pattern : manifest.getScanExcludes()) {
            if (pattern.matcher(entry.getName()).matches()) {
                return true;
            }
        }
        return false;
    }

}
