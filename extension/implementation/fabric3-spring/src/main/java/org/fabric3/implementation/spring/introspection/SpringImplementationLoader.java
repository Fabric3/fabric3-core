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
package org.fabric3.implementation.spring.introspection;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileFilter;
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

import org.fabric3.host.stream.Source;
import org.fabric3.host.stream.UrlSource;
import org.fabric3.implementation.spring.model.SpringComponentType;
import org.fabric3.implementation.spring.model.SpringImplementation;
import org.fabric3.spi.classloader.MultiParentClassLoader;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
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
    private LoaderHelper loaderHelper;

    public SpringImplementationLoader(@Reference SpringImplementationProcessor processor, @Reference LoaderHelper loaderHelper) {
        this.processor = processor;
        this.loaderHelper = loaderHelper;
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

        loaderHelper.loadPolicySetsAndIntents(implementation, reader, context);

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

                List<Source> sources = new ArrayList<Source>();
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
            File[] files = directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return (pathname.getName().endsWith(".xml"));
                }
            });
            if (files == null || files.length == 0) {
                InvalidValue error = new InvalidValue("Invalid file location: no application contexts found", startLocation, implementation);
                context.addError(error);
                return implementation;
            }
            try {
                contextLocations = new ArrayList<String>();
                List<Source> sources = new ArrayList<Source>();
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