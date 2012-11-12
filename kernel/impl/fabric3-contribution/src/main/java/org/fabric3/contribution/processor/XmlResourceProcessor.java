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
package org.fabric3.contribution.processor;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.stream.Source;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Processes an XML-based resource in a contribution, delegating to a an XMLIndexer to index the resource and a Loader to load it based on the root
 * element QName.
 */
@EagerInit
public class XmlResourceProcessor implements ResourceProcessor {
    private ProcessorRegistry processorRegistry;
    private XmlResourceElementLoaderRegistry elementLoaderRegistry;
    private XmlIndexerRegistry indexerRegistry;
    private XMLInputFactory xmlFactory;

    public XmlResourceProcessor(@Reference ProcessorRegistry processorRegistry,
                                @Reference XmlIndexerRegistry indexerRegistry,
                                @Reference XmlResourceElementLoaderRegistry elementLoaderRegistry,
                                @Reference XMLFactory xmlFactory) {
        this.processorRegistry = processorRegistry;
        this.elementLoaderRegistry = elementLoaderRegistry;
        this.indexerRegistry = indexerRegistry;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    @Destroy
    public void destroy() {
        processorRegistry.unregister(getContentType());
    }

    public String getContentType() {
        return "application/xml";
    }

    public void index(Resource resource, IntrospectionContext context) throws InstallException {
        Source source = resource.getSource();
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = source.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            if (skipToFirstTag(reader)) {
                return;
            }
            indexerRegistry.index(resource, reader, context);
        } catch (XMLStreamException e) {
            resource.setState(ResourceState.ERROR);
            // artifact is invalid, issue a warning
            Location location = reader.getLocation();
            InvalidXmlArtifact warning =
                    new InvalidXmlArtifact("Invalid XML in " + source.getSystemId() + ". The error reported was:\n " + e.getMessage(), location);
            context.addWarning(warning);
        } catch (IOException e) {
            throw new InstallException(e);
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
    }

    public void process(Resource resource, IntrospectionContext context) throws InstallException {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = resource.getSource().openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            if (reader.getLocation().getSystemId() == null) {
                reader = new LocationAwareXMLStreamReader(reader, resource.getSource().getSystemId());
            }
            if (skipToFirstTag(reader)) {
                resource.setState(ResourceState.PROCESSED);
                return;
            }
            elementLoaderRegistry.load(reader, resource, context);
            resource.setState(ResourceState.PROCESSED);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
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
    }

    private boolean skipToFirstTag(XMLStreamReader reader) throws XMLStreamException {
        while (reader.hasNext() && XMLStreamConstants.START_ELEMENT != reader.getEventType()) {
            reader.next();
        }
        return XMLStreamConstants.END_DOCUMENT == reader.getEventType();
    }

}
