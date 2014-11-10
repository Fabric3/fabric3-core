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
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.domain.DeploymentException;
import org.fabric3.api.host.domain.Domain;
import org.fabric3.api.host.domain.DomainJournal;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.runtime.event.DomainRecover;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.xml.XMLFactory;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Replays the domain journal when the controller synchronizes with the domain.
 * <p/>
 * The domain journal records the state of the domain as composites are included and undeployed. Replaying the journal has the affect of reinstating the logical
 * assembly to its prior state before the controller went offline (either as a result of a normal shutdown or system failure).
 */
@EagerInit
public class FSDomainReplayer implements Fabric3EventListener<DomainRecover> {
    private static final QName CONTRIBUTION = new QName(org.fabric3.api.Namespaces.F3, "contribution");
    private EventService eventService;
    private FSDomainReplayMonitor monitor;
    private XMLInputFactory inputFactory;
    private File domainLog;
    private Domain domain;

    public FSDomainReplayer(@Reference(name = "domain") Domain domain,
                            @Reference XMLFactory xmlFactory,
                            @Reference HostInfo hostInfo,
                            @Reference EventService eventService,
                            @Monitor FSDomainReplayMonitor monitor) {
        this.domain = domain;
        this.eventService = eventService;
        this.monitor = monitor;
        this.inputFactory = xmlFactory.newInputFactoryInstance();
        domainLog = new File(hostInfo.getDataDir(), "domain.xml");
    }

    @Init
    public void init() {
        eventService.subscribe(DomainRecover.class, this);
    }

    public void onEvent(DomainRecover event) {
        if (!domainLog.exists()) {
            return;
        }

        try {
            DomainJournal journal = parse();
            domain.recover(journal);
        } catch (FileNotFoundException | DeploymentException | XMLStreamException e) {
            monitor.error(e);
        }
    }

    /**
     * Reads the domain journal.
     *
     * @return th list of journal entries
     * @throws FileNotFoundException if the journal file does not exist
     * @throws XMLStreamException    if there is an error reading the journal
     */
    private DomainJournal parse() throws FileNotFoundException, XMLStreamException {
        FileInputStream fis = new FileInputStream(domainLog);
        BufferedInputStream stream = new BufferedInputStream(fis);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(stream);
        List<URI> contributions = new ArrayList<>();
        try {
            while (true) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                        if (CONTRIBUTION.equals(reader.getName())) {
                            String uri = reader.getAttributeValue(null, "uri");
                            if (uri == null) {
                                Location location = reader.getLocation();
                                int line = location.getLineNumber();
                                int col = location.getColumnNumber();
                                monitor.errorMessage("URI attribute missing in domain journal [" + line + "," + col + "]");
                                continue;
                            }
                            contributions.add(URI.create(uri));
                        }
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        return new DomainJournal(contributions);
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

}