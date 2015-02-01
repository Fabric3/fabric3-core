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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.api.host.util.FileHelper;
import org.fabric3.spi.contribution.ContentTypeResolver;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionManifest;
import org.fabric3.spi.contribution.JavaArtifactIntrospector;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.archive.ArchiveContributionHandler;
import org.fabric3.spi.contribution.archive.ArtifactResourceCallback;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.oasisopen.sca.annotation.Reference;
import static org.fabric3.spi.contribution.Constants.EXPLODED_CONTENT_TYPE;

/**
 * Handles exploded archives on a filesystem.
 */
public class ExplodedArchiveContributionHandler implements ArchiveContributionHandler {
    private Loader loader;
    private List<JavaArtifactIntrospector> artifactIntrospectors = Collections.emptyList();
    private final ContentTypeResolver contentTypeResolver;

    public ExplodedArchiveContributionHandler(@Reference Loader loader, @Reference ContentTypeResolver contentTypeResolver) {
        this.loader = loader;
        this.contentTypeResolver = contentTypeResolver;
    }

    @Reference
    public void setArtifactIntrospectors(List<JavaArtifactIntrospector> introspectors) {
        this.artifactIntrospectors = introspectors;
    }

    public boolean canProcess(Contribution contribution) {
        URL location = contribution.getLocation();
        if (location == null || !"file".equals(location.getProtocol())) {
            return false;
        }
        File file = new File(location.getFile());
        String contentType = contribution.getContentType();
        return file.isDirectory() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip") || EXPLODED_CONTENT_TYPE.equals(contentType));
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws Fabric3Exception {
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
                throw new Fabric3Exception(e);
            }
        } catch (MalformedURLException e) {
            // ignore no manifest found
        }
    }

    public void iterateArtifacts(Contribution contribution, ArtifactResourceCallback callback, IntrospectionContext context) throws Fabric3Exception {
        File root = FileHelper.toFile(contribution.getLocation());
        iterateArtifactsRecursive(root, root, contribution, callback, context);
    }

    protected void iterateArtifactsRecursive(File dir, File root, Contribution contribution, ArtifactResourceCallback callback, IntrospectionContext context)
            throws Fabric3Exception {
        File[] files = dir.listFiles();
        ContributionManifest manifest = contribution.getManifest();
        for (File file : files) {
            if (file.isDirectory()) {
                iterateArtifactsRecursive(file, root, contribution, callback, context);
            } else {
                try {
                    String name = file.getName();
                    if (name.equals("sca-contribution.xml")) {
                        // don't index the manifest
                        continue;
                    }

                    if (exclude(file, root, manifest)) {
                        continue;
                    }

                    boolean isClass = file.getName().endsWith(".class");
                    if (isClass) {
                        name = getRelativeName(file, root).replace(File.separator, ".").substring(0, file.getName().length() - 6);
                        try {
                            Class<?> clazz = context.getClassLoader().loadClass(name);

                            URL entryUrl = file.toURI().toURL();
                            Resource resource = null;
                            for (JavaArtifactIntrospector introspector : artifactIntrospectors) {
                                resource = introspector.inspect(clazz, entryUrl, contribution, context);
                                if (resource != null) {
                                    break;
                                }
                            }

                            if (resource == null) {
                                continue;
                            }
                            contribution.addResource(resource);
                            callback.onResource(resource);
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            // ignore since the class may reference another class not present in the contribution
                        }

                    } else {

                        String contentType = contentTypeResolver.getContentType(name);
                        // skip entry if we don't recognize the content type
                        if (contentType == null) {
                            continue;
                        }
                        URL entryUrl = file.toURI().toURL();
                        UrlSource source = new UrlSource(entryUrl);
                        Resource resource = new Resource(contribution, source, contentType);
                        contribution.addResource(resource);
                        callback.onResource(resource);
                    }
                } catch (MalformedURLException e) {
                    throw new Fabric3Exception(e);
                }
            }
        }

    }

    private boolean exclude(File file, File root, ContributionManifest manifest) {
        // construct a file name relative to the root directory as excludes are relative to the archive root
        String relativeName = getRelativeName(file, root);
        for (Pattern pattern : manifest.getScanExcludes()) {
            if (pattern.matcher(relativeName).matches()) {
                return true;
            }
        }
        return false;
    }

    private String getRelativeName(File file, File root) {
        return file.toURI().toString().substring(root.toURI().toString().length());
    }

}
