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
package org.fabric3.implementation.spring.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.implementation.spring.model.SpringComponentType;
import org.fabric3.implementation.spring.model.SpringImplementation;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a Spring component implementation in a composite.
 */
@EagerInit
public class SpringImplementationLoader extends AbstractValidatingTypeLoader<SpringImplementation> {
    private SpringImplementationProcessor processor;

    public SpringImplementationLoader(@Reference SpringImplementationProcessor processor) {
        this.processor = processor;
        addAttributes("location", "requires", "policySets");
    }

    public SpringImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        SpringImplementation implementation = new SpringImplementation();

        validateAttributes(reader, context, implementation);

        ClassLoader classLoader = context.getClassLoader();
        updateClassLoader(classLoader);
        String locationAttr = reader.getAttributeValue(null, "location");
        if (locationAttr == null) {
            MissingAttribute failure = new MissingAttribute("The location attribute was not specified", startLocation);
            context.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return implementation;
        }
        implementation.setLocation(locationAttr);

        LoaderUtil.skipToEndElement(reader);

        URL resource = classLoader.getResource(locationAttr);

        if (resource == null) {
            InvalidValue error = new InvalidValue("Spring resource not found: " + locationAttr, startLocation, implementation);
            context.addError(error);
            return implementation;
        }

        List<String> contextLocations;
        Source source;
        if (locationAttr.endsWith(".jar")) {
            // if the location is a jar file, resolve the app context by introspecting the manifest header
            try {
                contextLocations = resolveAppContextLocationInJar(resource);
                if (contextLocations == null || contextLocations.isEmpty()) {
                    InvalidValue error = new InvalidValue("Invalid jar: missing an application context", startLocation, implementation);
                    context.addError(error);
                    return implementation;
                }
                String externalForm = resource.toExternalForm();

                List<Source> sources = new ArrayList<>();
                for (int i = 1; i < contextLocations.size(); i++) {
                    sources.add(new UrlSource(new URL("jar:" + externalForm + contextLocations.get(i))));
                }
                source = new MultiSource(new URL("jar:" + externalForm + contextLocations.get(0)), sources);
                implementation.setLocationType(SpringImplementation.LocationType.JAR);
            } catch (IOException e) {
                InvalidValue error = new InvalidValue("Invalid jar location", startLocation, e, implementation);
                context.addError(error);
                return implementation;
            }

        } else if (new File(resource.getPath()).isDirectory()) {
            // location is a directory
            File directory = new File(resource.getPath());
            File[] files = directory.listFiles(pathname -> (pathname.getName().endsWith(".xml")));
            if (files == null || files.length == 0) {
                InvalidValue error = new InvalidValue("Invalid file location: no application contexts found", startLocation, implementation);
                context.addError(error);
                return implementation;
            }
            try {
                contextLocations = new ArrayList<>();
                List<Source> sources = new ArrayList<>();
                for (File file : files) {
                    contextLocations.add(file.getName());
                }
                for (int i = 1; i < files.length; i++) {
                    sources.add(new UrlSource(files[i].toURI().toURL()));
                }
                source = new MultiSource(files[0].toURI().toURL(), sources);
            } catch (MalformedURLException e) {
                InvalidValue error = new InvalidValue("Invalid file location:", startLocation, e, implementation);
                context.addError(error);
                return implementation;
            }
            implementation.setLocationType(SpringImplementation.LocationType.DIRECTORY);
        } else {
            // location is a file
            contextLocations = Collections.singletonList(locationAttr);
            source = new UrlSource(resource);
        }

        implementation.setContextLocations(contextLocations);

        SpringComponentType type = processor.introspect(source, context);
        implementation.setComponentType(type);
        return implementation;

    }

    private List<String> resolveAppContextLocationInJar(URL resource) throws IOException {
        String externalForm = resource.toExternalForm();

        try {
            URL jarUrl = new URL("jar:" + externalForm + "!/META-INF/MANIFEST.MF");
            InputStream manifestStream = jarUrl.openStream();
            Manifest jarManifest = new Manifest(manifestStream);
            String relativeLocation = jarManifest.getMainAttributes().getValue("Spring-Context");
            if (relativeLocation != null) {
                return Collections.singletonList("!/" + relativeLocation);

            }
        } catch (IOException e) {
            // ignore, no manifest
        }
        JarInputStream stream = new JarInputStream(resource.openStream());
        JarEntry entry;
        while ((entry = stream.getNextJarEntry()) != null) {
            if (entry.getName().contains("/spring/") && entry.getName().endsWith(".xml")) {
                return Collections.singletonList("!/" + entry.getName());
            }

        }
        return null;
    }

    /**
     * Make Spring classes available to the contribution classloader. This is required since user classes may extend Spring classes.
     *
     * @param classLoader the application classloader.
     */
    private void updateClassLoader(ClassLoader classLoader) {
        if (!(classLoader instanceof MultiParentClassLoader)) {
            return;
        }
        MultiParentClassLoader loader = (MultiParentClassLoader) classLoader;
        ClassLoader springClassLoader = getClass().getClassLoader();
        for (ClassLoader parent : loader.getParents()) {
            if (parent == springClassLoader) {
                return;
            }
        }
        loader.addParent(springClassLoader);
    }

}