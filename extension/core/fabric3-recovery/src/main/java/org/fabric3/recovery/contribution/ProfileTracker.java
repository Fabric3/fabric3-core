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
package org.fabric3.recovery.contribution;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.runtime.event.EventService;
import org.fabric3.spi.runtime.event.Fabric3EventListener;
import org.fabric3.spi.runtime.event.RuntimeRecover;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Records profiles installed in the runtime.
 */
@Service({ContributionServiceListener.class, Fabric3EventListener.class})
@EagerInit
public class ProfileTracker implements ContributionServiceListener, Fabric3EventListener<RuntimeRecover> {
    private static final QName CONTRIBUTION = new QName(org.fabric3.api.Namespaces.F3, "contribution");
    private XMLInputFactory inputFactory;
    private XMLOutputFactory outputFactory;
    private File repositoryIndex;
    private ProfileTrackerMonitor monitor;
    private EventService eventService;
    private MetaDataStore store;
    private Map<URI, List<URI>> mappings;

    public ProfileTracker(@Reference XMLFactory factory,
                          @Reference EventService eventService,
                          @Reference HostInfo hostInfo,
                          @Reference MetaDataStore store,
                          @Monitor ProfileTrackerMonitor monitor) {
        this.eventService = eventService;
        this.store = store;
        this.inputFactory = factory.newInputFactoryInstance();
        this.outputFactory = factory.newOutputFactoryInstance();
        this.monitor = monitor;
        repositoryIndex = new File(hostInfo.getDataDir(), "profiles.xml");
        mappings = new HashMap<>();
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeRecover.class, this);
    }

    public void onEvent(RuntimeRecover event) {
        if (!repositoryIndex.exists()) {
            return;
        }
        try {
            mappings = parse();
            List<URI> deleted = new ArrayList<>();
            for (Map.Entry<URI, List<URI>> entry : mappings.entrySet()) {
                URI uri = entry.getKey();
                Contribution contribution = store.find(uri);
                if (contribution == null) {
                    deleted.add(uri);
                } else {
                    contribution.addProfiles(entry.getValue());
                }

            }
            for (URI uri : deleted) {
                mappings.remove(uri);
            }
            persist();
        } catch (FileNotFoundException | InvalidRepositoryIndexException | XMLStreamException e) {
            monitor.error(e);
        }
    }

    public void onStore(Contribution contribution) {
        List<URI> profiles = contribution.getProfiles();
        if (profiles.isEmpty()) {
            return;
        }
        URI uri = contribution.getUri();
        mappings.put(uri, profiles);
        persist();
    }

    public void onProcessManifest(Contribution contribution) {
        // no-op
    }

    public void onInstall(Contribution contribution) {
        update(contribution);

    }

    public void onUpdate(Contribution contribution) {
        update(contribution);
    }

    public void onUninstall(Contribution contribution) {
        update(contribution);
    }

    public void onRemove(Contribution contribution) {
        if (!contribution.isPersistent()) {
            return;
        }
        mappings.remove(contribution.getUri());
        persist();
    }

    /**
     * Updates the repository index based on the changed contribution.
     *
     * @param contribution the changed contribution
     */
    private void update(Contribution contribution) {
        if (!contribution.isPersistent()) {
            return;
        }
        mappings.put(contribution.getUri(), contribution.getProfiles());
        persist();
    }

    private void persist() {
        if (mappings.isEmpty()) {
            return;
        }
        BufferedOutputStream stream = null;
        try {
            FileOutputStream fos = new FileOutputStream(repositoryIndex);
            stream = new BufferedOutputStream(fos);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);
            writer.writeStartDocument();
            writer.writeStartElement("profiles");
            writer.writeDefaultNamespace(org.fabric3.api.Namespaces.F3);
            for (Map.Entry<URI, List<URI>> entry : mappings.entrySet()) {
                writer.writeStartElement("contribution");
                writer.writeAttribute("uri", entry.getKey().toString());
                List<URI> profiles = entry.getValue();
                if (!profiles.isEmpty()) {
                    StringBuilder b = new StringBuilder();
                    for (int i = 0; i < profiles.size() - 1; i++) {
                        URI profile = entry.getValue().get(i);
                        b.append(profile.toString()).append(" ");
                    }
                    b.append(profiles.get(profiles.size() - 1));
                    writer.writeAttribute("profiles", b.toString());
                }
                writer.writeEndElement();
            }
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

    private Map<URI, List<URI>> parse() throws FileNotFoundException, XMLStreamException, InvalidRepositoryIndexException {
        Map<URI, List<URI>> profileMappings = new HashMap<>();
        FileInputStream fis = new FileInputStream(repositoryIndex);
        BufferedInputStream stream = new BufferedInputStream(fis);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(stream);
        try {
            while (true) {
                switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    if (CONTRIBUTION.equals(reader.getName())) {
                        String uriStr = reader.getAttributeValue(null, "uri");
                        if (uriStr == null) {
                            throw createException("URI attribute missing ", reader, null);
                        }
                        URI uri = URI.create(uriStr);
                        List<URI> profiles = new ArrayList<>();
                        String profilesStr = reader.getAttributeValue(null, "profiles");
                        if (profilesStr != null) {
                            String[] tokens = profilesStr.split(" ");
                            for (String token : tokens) {
                                try {
                                    profiles.add(new URI(token));
                                } catch (URISyntaxException e) {
                                    throw createException("Invalid profile name:" + token, reader, e);
                                }
                            }
                        }
                        profileMappings.put(uri, profiles);
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    return profileMappings;
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

    private InvalidRepositoryIndexException createException(String message, XMLStreamReader reader, Exception e) {
        Location location = reader.getLocation();
        String msg = message + "[" + location.getLineNumber() + "," + location.getColumnNumber() + "]";
        if (e == null) {
            return new InvalidRepositoryIndexException(msg);
        } else {
            return new InvalidRepositoryIndexException(msg, e);
        }
    }
}