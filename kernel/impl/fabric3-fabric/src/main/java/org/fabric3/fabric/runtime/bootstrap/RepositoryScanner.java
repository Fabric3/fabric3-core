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
package org.fabric3.fabric.runtime.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import org.fabric3.host.Namespaces;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.SyntheticContributionSource;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ScanResult;

/**
 * Scans a repository for extension and user contributions.
 *
 * @version $Rev$ $Date$
 */
public class RepositoryScanner {
    private static final String MANIFEST_PATH = "META-INF/sca-contribution.xml";
    DocumentBuilderFactory documentBuilderFactory;

    public RepositoryScanner() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    /**
     * Scans a repository directory for contributions.
     *
     * @param directory the directory
     * @return the contributions grouped by user and extension contributions
     * @throws InitializationException if there is an error scanning teh directory
     */
    public ScanResult scan(File directory) throws InitializationException {

        File[] files = directory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                // skip directories and files beginning with '.'
                return !pathname.isDirectory() && !pathname.getName().startsWith(".");
            }
        });
        ScanResult result = new ScanResult();
        Map<URL, ContributionSource> toScan = new HashMap<URL, ContributionSource>();
        for (File file : files) {
            try {
                URI uri = URI.create(file.getName());
                URL location = file.toURI().toURL();
                ContributionSource source = new FileContributionSource(uri, location, -1);
                if (!file.getName().endsWith(".jar")) {
                    // if the file is not a JAR, it must be a user contribution
                    result.addUserContribution(source);
                } else {
                    toScan.put(location, source);
                }
            } catch (MalformedURLException e) {
                throw new InitializationException("Error loading contribution", file.getName(), e);
            }
        }


        URL[] urls = toScan.keySet().toArray(new URL[toScan.size()]);
        URLClassLoader urlClassLoader = new URLClassLoader(urls);
        Enumeration<URL> scannedManifests;
        try {
            scannedManifests = urlClassLoader.getResources(MANIFEST_PATH);
        } catch (IOException e) {
            throw new InitializationException("Error scanning repository", e);
        }

        Set<URL> manifests = new HashSet<URL>();

        while (scannedManifests.hasMoreElements()) {
            URL manifestUrl = scannedManifests.nextElement();
            URL contributionUrl = getContributionUrl(manifestUrl);
            boolean extension = isExtension(manifestUrl);
            if (extension) {
                result.addExtensionContribution(toScan.get(contributionUrl));
            } else {
                result.addUserContribution(toScan.get(contributionUrl));
            }
            manifests.add(contributionUrl);
        }

        // Make another pass and categorize all contributions without an SCA manifest as extensions. This is safe as they cannot contain deployable
        // components and hence only contain sharable artifacts.
        for (Map.Entry<URL, ContributionSource> entry : toScan.entrySet()) {
            if (!manifests.contains(entry.getKey())) {
                result.addExtensionContribution(entry.getValue());
            }
        }

        // create synthetic contributions from directories contained in /repository
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                try {
                    URI uri = URI.create("f3-" + file.getName());
                    URL location = file.toURI().toURL();
                    ContributionSource source = new SyntheticContributionSource(uri, location);
                    result.addExtensionContribution(source);
                } catch (MalformedURLException e) {
                    throw new InitializationException(e);
                }
            }
        }
        return result;


    }

    /**
     * Checks whether a contribution containing the manifest is an extension.
     *
     * @param manifestUrl the URL of the contribution manifest
     * @return true if the contribution is an extension
     * @throws InitializationException if there is an error scanning the manifest
     */
    private boolean isExtension(URL manifestUrl) throws InitializationException {
        try {
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();

            InputStream stream = manifestUrl.openStream();
            Document document = db.parse(stream);
            stream.close();

            String extension = document.getDocumentElement().getAttributeNS(Namespaces.CORE, "extension");
            return extension != null && !"".equals(extension.trim());
        } catch (IOException e) {
            throw new InitializationException(e);
        } catch (ParserConfigurationException e) {
            throw new InitializationException(e);
        } catch (SAXException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Computes the contribution URL from the manifest URL.
     *
     * @param manifestUrl the manifest URL
     * @return the contribution URL
     * @throws InitializationException if there is an error computing the URL
     */
    private URL getContributionUrl(URL manifestUrl) throws InitializationException {

        String externalForm = manifestUrl.toExternalForm();
        String protocol = manifestUrl.getProtocol();
        String url;

        if ("jar".equals(protocol)) {
            url = externalForm.substring(0, externalForm.indexOf("!/" + MANIFEST_PATH));
            // Strip the jar protocol
            url = url.substring(4);
        } else {
            url = externalForm.substring(0, externalForm.indexOf(MANIFEST_PATH));
        }
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new InitializationException(e);
        }

    }


}
