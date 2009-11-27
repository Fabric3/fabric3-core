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
package org.fabric3.implementation.web.introspection;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.MissingResource;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Default implementation of WebXmlIntrospector.
 *
 * @version $Rev$ $Date$
 */
public class WebXmlIntrospectorImpl implements WebXmlIntrospector {
    private static final QNameSymbol WEB_APP_NO_NAMESPACE = new QNameSymbol(new QName(null, "web-app"));
    private static final QNameSymbol WEB_APP_NAMESPACE = new QNameSymbol(new QName("http://java.sun.com/xml/ns/j2ee", "web-app"));

    private MetaDataStore store;
    private XMLInputFactory xmlFactory;

    public WebXmlIntrospectorImpl(@Reference MetaDataStore store, @Reference XMLFactory factory) {
        this.store = store;
        this.xmlFactory = factory.newInputFactoryInstance();
    }

    public List<Class<?>> introspectArtifactClasses(IntrospectionContext context) {
        List<Class<?>> artifacts = new ArrayList<Class<?>>();
        ClassLoader cl = context.getClassLoader();
        Resource resource = store.resolveContainingResource(context.getContributionUri(), WEB_APP_NAMESPACE);
        if (resource == null) {
            resource = store.resolveContainingResource(context.getContributionUri(), WEB_APP_NO_NAMESPACE);
            if (resource == null) {
                // tolerate no web.xml
                return artifacts;
            }
        }
        InputStream xmlStream = null;
        try {
            xmlStream = resource.getUrl().openStream();
            XMLStreamReader reader = xmlFactory.createXMLStreamReader(xmlStream);
            while (true) {
                // only match on local part since namespaces may be omitted
                int event = reader.next();
                switch (event) {
                case START_ELEMENT:
                    String name = reader.getName().getLocalPart();
                    if (name.equals("servlet-class")) {
                        String className = reader.getElementText();
                        try {
                            artifacts.add(cl.loadClass(className.trim()));
                        } catch (ClassNotFoundException e) {
                            MissingResource failure = new MissingResource("Servlet class not found: " + className, className);
                            context.addError(failure);
                        }
                    } else if (name.equals("filter-class")) {
                        String className = reader.getElementText();
                        try {
                            artifacts.add(cl.loadClass(className.trim()));
                        } catch (ClassNotFoundException e) {
                            MissingResource failure = new MissingResource("Filter class not found: " + className, className);
                            context.addError(failure);
                        }
                    }
                    break;
                case END_ELEMENT:
                    if (reader.getName().getLocalPart().equals("web-app")) {
                        return artifacts;
                    }
                    break;
                case END_DOCUMENT:
                    return artifacts;
                }
            }
        } catch (XMLStreamException e) {
            InvalidWebManifest failure = new InvalidWebManifest("Error reading web.xml", e);
            context.addError(failure);
        } catch (IOException e) {
            InvalidWebManifest failure = new InvalidWebManifest("Error reading web.xml", e);
            context.addError(failure);
        } finally {
            try {
                if (xmlStream != null) {
                    xmlStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return artifacts;

    }
}
