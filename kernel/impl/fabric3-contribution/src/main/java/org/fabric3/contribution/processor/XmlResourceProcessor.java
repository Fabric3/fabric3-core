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

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.ResourceState;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.xml.LocationAwareXMLStreamReader;
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
            Location location = null;
            if (reader != null) {
                location = reader.getLocation();
            }
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
        } catch (XMLStreamException | IOException e) {
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
