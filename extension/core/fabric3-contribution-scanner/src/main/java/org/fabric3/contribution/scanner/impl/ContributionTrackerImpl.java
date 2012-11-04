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
package org.fabric3.contribution.scanner.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Init;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.host.Namespaces;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.xml.XMLFactory;

/**
 *
 */
public class ContributionTrackerImpl implements ContributionTracker {
    private static final String CONTRIBUTION = "contribution";

    private XMLFactory xmlFactory;
    private HostInfo info;
    private ContributionTrackerMonitor monitor;

    private File journal;
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private Set<String> tracked = new HashSet<String>();

    public ContributionTrackerImpl(@Reference XMLFactory xmlFactory, @Reference HostInfo info, @Monitor ContributionTrackerMonitor monitor) {
        this.xmlFactory = xmlFactory;
        this.info = info;
        this.monitor = monitor;
    }

    @Init
    public void init() throws XMLStreamException, FileNotFoundException {
        journal = new File(info.getDataDir(), "contributions.xml");
        inputFactory = xmlFactory.newInputFactoryInstance();
        outputFactory = xmlFactory.newOutputFactoryInstance();
        read();
    }

    public synchronized void addResource(String name) {
        tracked.add(name);
        persist();
    }

    public synchronized void removeResource(String name) {
        tracked.remove(name);
        persist();
    }

    public synchronized boolean isTracked(String name) {
        return tracked.contains(name);
    }

    /**
     * Reads the contribution journal.
     *
     * @throws XMLStreamException    if there is an error reading the journal
     * @throws FileNotFoundException if the journal file cannot be opened
     */
    private void read() throws XMLStreamException, FileNotFoundException {
        if (!journal.exists()) {
            return;
        }
        FileInputStream fis = new FileInputStream(journal);
        BufferedInputStream stream = new BufferedInputStream(fis);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(stream);
        try {
            while (true) {
                switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (CONTRIBUTION.equals(reader.getLocalName())) {
                        String name = reader.getAttributeValue(null, "name");
                        if (name == null) {
                            Location location = reader.getLocation();
                            int line = location.getLineNumber();
                            int col = location.getColumnNumber();
                            monitor.errorMessage("Name attribute missing in contribution journal [" + line + "," + col + "]");
                            continue;
                        }
                        tracked.add(name);
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return;
                }

            }
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                // ignore
            }
        }

    }


    private void persist() {
        BufferedOutputStream stream = null;
        try {
            FileOutputStream fos = new FileOutputStream(journal);
            stream = new BufferedOutputStream(fos);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);
            writer.writeStartDocument();
            writer.writeStartElement("contributions");
            writer.writeDefaultNamespace(Namespaces.F3);
            for (String entry : tracked) {
                writer.writeStartElement("contribution");
                writer.writeAttribute("name", entry);
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            if (journal.exists()) {
                journal.delete();
            }
            monitor.error(e);
        } catch (IOException e) {
            if (journal.exists()) {
                journal.delete();
            }
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
