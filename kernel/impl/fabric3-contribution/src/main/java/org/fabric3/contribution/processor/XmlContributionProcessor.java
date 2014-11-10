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

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.host.stream.Source;
import org.fabric3.api.host.stream.UrlSource;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.contribution.xml.XmlProcessorRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.xml.XMLFactory;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Processes an XML-based contribution. The implementation dispatches to a specific XmlProcessor based on the QName of the document element.
 */
@EagerInit
public class XmlContributionProcessor implements ContributionProcessor {
    private static final String CONTENT_TYPE = "application/xml";

    private XMLInputFactory xmlFactory;
    private ProcessorRegistry processorRegistry;
    private XmlProcessorRegistry xmlProcessorRegistry;
    private XmlIndexerRegistry xmlIndexerRegistry;

    public XmlContributionProcessor(@Reference ProcessorRegistry processorRegistry,
                                    @Reference XmlProcessorRegistry xmlProcessorRegistry,
                                    @Reference XmlIndexerRegistry xmlIndexerRegistry,
                                    @Reference XMLFactory xmlFactory) {
        this.processorRegistry = processorRegistry;
        this.xmlProcessorRegistry = xmlProcessorRegistry;
        this.xmlIndexerRegistry = xmlIndexerRegistry;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        processorRegistry.register(this);
    }

    @Destroy
    public void destroy() {
        processorRegistry.unregister(this);
    }

    public boolean canProcess(Contribution contribution) {
        return CONTENT_TYPE.equals(contribution.getContentType());
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        // no-op as XML contributions do not contain manifest headers
    }

    public void index(Contribution contribution, IntrospectionContext context) throws InstallException {
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            URL locationURL = contribution.getLocation();
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            URL url = contribution.getLocation();
            Source source = new UrlSource(url);
            Resource resource = new Resource(contribution, source, "application/xml");
            xmlIndexerRegistry.index(resource, reader, context);
            contribution.addResource(resource);
        } catch (IOException e) {
            String uri = contribution.getUri().toString();
            throw new InstallException("Error processing contribution " + uri, e);
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            if (e.getLocation() == null) {
                throw new InstallException("Error processing contribution " + uri, e);
            }
            int line = e.getLocation().getLineNumber();
            int col = e.getLocation().getColumnNumber();
            throw new InstallException("Error processing contribution " + uri + " [" + line + "," + col + "]", e);
        } finally {
            close(stream, reader);
        }
    }

    public void process(Contribution contribution, IntrospectionContext context) throws InstallException {
        URL locationURL = contribution.getLocation();
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            stream = locationURL.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();
            xmlProcessorRegistry.process(contribution, reader, context);
        } catch (IOException e) {
            String uri = contribution.getUri().toString();
            throw new InstallException("Error processing contribution " + uri, e);
        } catch (XMLStreamException e) {
            String uri = contribution.getUri().toString();
            int line = e.getLocation().getLineNumber();
            int col = e.getLocation().getColumnNumber();
            throw new InstallException("Error processing contribution " + uri + " [" + line + "," + col + "]", e);
        } finally {
            close(stream, reader);
        }
    }

    private void close(InputStream stream, XMLStreamReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
