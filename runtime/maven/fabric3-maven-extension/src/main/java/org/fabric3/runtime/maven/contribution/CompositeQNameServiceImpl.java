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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.Constants;
import org.fabric3.host.contribution.ContributionNotFoundException;
import org.fabric3.runtime.maven.CompositeQNameService;
import org.fabric3.runtime.maven.InvalidResourceException;
import org.fabric3.model.type.component.Composite;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.xml.XMLFactory;

/**
 * @version $Rev$ $Date$
 */
public class CompositeQNameServiceImpl implements CompositeQNameService {
    private MetaDataStore store;
    private XMLInputFactory xmlFactory;

    public CompositeQNameServiceImpl(@Reference MetaDataStore store, @Reference XMLFactory factory) {
        this.store = store;
        this.xmlFactory = factory.newInputFactoryInstance();
    }

    public QName getQName(URI uri, URL url) throws ContributionNotFoundException, InvalidResourceException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            throw new ContributionNotFoundException("Contribution not found: " + uri);
        }
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                throw new InvalidResourceException("Composite name not specified in : " + url);
            }
            Resource resource = new Resource(url, Constants.COMPOSITE_CONTENT_TYPE);
            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            QName compositeName = new QName(targetNamespace, name);
            QNameSymbol symbol = new QNameSymbol(compositeName);
            ResourceElement<QNameSymbol, Composite> element = new ResourceElement<QNameSymbol, Composite>(symbol);
            resource.addResourceElement(element);
            contribution.addResource(resource);
        } catch (XMLStreamException e) {
            throw new InvalidResourceException("Error reading " + url, e);
        } catch (IOException e) {
            throw new InvalidResourceException("Error reading " + url, e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }


        for (Resource resource : contribution.getResources()) {
            if (url.equals(resource.getUrl())) {
                if (resource.getResourceElements().size() != 1) {
                    throw new InvalidResourceException("Resource must contain one resource element");
                }
                ResourceElement<?, ?> element = resource.getResourceElements().get(0);
                Symbol symbol = element.getSymbol();
                if (symbol instanceof QNameSymbol) {
                    return ((QNameSymbol) symbol).getKey();
                } else {
                    throw new InvalidResourceException("Resource symbol is not of expected type:" + symbol);
                }
            }
        }
        return null;
    }
}
