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
package org.fabric3.recovery.domain;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.xml.XMLFactory;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Records the current domain state to a journal so it may be replayed when a controller comes back online and resyncs with the domain.
 * <p/>
 * Deployed contributions and composites are written to a file system based journal as the state of the domain changes (i.e. deployments and undeployments are
 * made). On recovery, the journal can be read to reconstitute the current state of the domain.
 */
@EagerInit
public class FSDeployTracker implements DeployListener {
    private File domainLog;
    private XMLOutputFactory outputFactory;
    private DeployTrackerMonitor monitor;
    private Set<URI> contributions;

    public FSDeployTracker(@Reference XMLFactory factory, @Reference HostInfo info, @Monitor DeployTrackerMonitor monitor) {
        this.monitor = monitor;
        this.outputFactory = factory.newOutputFactoryInstance();
        this.contributions = new LinkedHashSet<>();
        domainLog = new File(info.getDataDir(), "domain.xml");
    }

    public void onDeploy(URI uri) {
        contributions.add(uri);
        persist();
    }

    public void onUnDeploy(URI uri) {
        contributions.remove(uri);
        persist();
    }

    public void onDeploy(QName included) {
        // no-op
    }

    public void onUndeploy(QName undeployed) {
        // no-op
    }

    public void onDeployCompleted(URI uri) {
        // no-op
    }

    public void onUnDeployCompleted(URI contribution) {
        // no-op
    }

    public void onDeployCompleted(QName deployable) {
        // no-op
    }

    public void onUndeployCompleted(QName undeployed) {
        // no-op
    }

    private void persist() {
        BufferedOutputStream stream = null;
        try {
            FileOutputStream fos = new FileOutputStream(domainLog);
            stream = new BufferedOutputStream(fos);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);
            writer.writeStartDocument();
            writer.writeStartElement("domain");
            writer.writeDefaultNamespace(org.fabric3.api.Namespaces.F3);
            writeContributions(writer);
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (FileNotFoundException | XMLStreamException e) {
            monitor.error(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private void writeContributions(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("contributions");
        for (URI uri : contributions) {
            writer.writeStartElement("contribution");
            writer.writeAttribute("uri", uri.toString());
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

}
