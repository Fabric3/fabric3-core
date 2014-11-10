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
import org.fabric3.api.host.runtime.HostInfo;
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
    private Set<String> tracked = new HashSet<>();

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
            writer.writeDefaultNamespace(org.fabric3.api.Namespaces.F3);
            for (String entry : tracked) {
                writer.writeStartElement("contribution");
                writer.writeAttribute("name", entry);
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (XMLStreamException | IOException e) {
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
