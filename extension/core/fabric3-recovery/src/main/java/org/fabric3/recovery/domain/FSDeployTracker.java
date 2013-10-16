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
package org.fabric3.recovery.domain;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.Namespaces;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.domain.DeployListener;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Records the current domain state to a journal so it may be replayed when a controller comes back online and resyncs with the domain.
 * <p/>
 * Deployed contributions and composites are written to a file system based journal as the state of the domain changes (i.e. deployments and
 * undeployments are made). On recovery, the journal can be read to reconstitute the current state of the domain.
 */
@EagerInit
public class FSDeployTracker implements DeployListener {
    private static final String NO_PLAN = "";
    private File domainLog;
    private XMLOutputFactory outputFactory;
    private DeployTrackerMonitor monitor;
    private List<URI> contributions;
    private Map<QName, String> deployables;

    public FSDeployTracker(@Reference XMLFactory factory, @Reference HostInfo info, @Monitor DeployTrackerMonitor monitor) {
        this.monitor = monitor;
        this.outputFactory = factory.newOutputFactoryInstance();
        this.contributions = new ArrayList<URI>();
        this.deployables = new HashMap<QName, String>();
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

    public void onDeploy(QName included, String plan) {
        if (plan == null) {
            plan = NO_PLAN;
        }
        deployables.put(included, plan);
        persist();
    }

    public void onUndeploy(QName undeployed) {
        deployables.remove(undeployed);
        persist();
    }

    public void onDeployCompleted(URI uri) {
        // no-op
    }

    public void onUnDeployCompleted(URI contribution) {
        // no-op
    }

    public void onDeployCompleted(QName deployable, String plan) {
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
            writer.writeDefaultNamespace(Namespaces.F3);
            writeContributions(writer);
            writeDeployables(writer);
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (FileNotFoundException e) {
            monitor.error(e);
        } catch (XMLStreamException e) {
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

    private void writeDeployables(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement("deployables");
        for (Map.Entry<QName, String> entry : deployables.entrySet()) {
            QName deployable = entry.getKey();
            String plan = entry.getValue();
            writer.writeStartElement("deployable");
            writer.writeAttribute("namespace", deployable.getNamespaceURI());
            writer.writeAttribute("name", deployable.getLocalPart());
            if (plan != NO_PLAN) {
                writer.writeAttribute("plan", plan);
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
    }

}
