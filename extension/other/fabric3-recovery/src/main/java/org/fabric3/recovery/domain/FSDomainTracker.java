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
package org.fabric3.recovery.domain;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.host.Namespaces;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.domain.DomainListener;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Records the current domain state to a journal so it may be replayed when a controller comes back online and resyncs with the domain.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FSDomainTracker implements DomainListener {
    private static final String NO_PLAN = "";
    private File domainLog;
    private XMLOutputFactory outputFactory;
    private FSDomainTrackerMonitor monitor;
    private Map<QName, String> deployables;

    public FSDomainTracker(@Reference XMLFactory factory, @Reference HostInfo info, @Monitor FSDomainTrackerMonitor monitor) {
        this.monitor = monitor;
        this.outputFactory = factory.newOutputFactoryInstance();
        this.deployables = new HashMap<QName, String>();
        domainLog = new File(info.getDataDir(), "domain.xml");
    }

    public void onInclude(QName included, String plan) {
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

    private void persist() {
        BufferedOutputStream stream = null;
        try {
            FileOutputStream fos = new FileOutputStream(domainLog);
            stream = new BufferedOutputStream(fos);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);
            writer.writeStartDocument();
            writer.writeStartElement("domain");
            writer.writeDefaultNamespace(Namespaces.CORE);
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

}
