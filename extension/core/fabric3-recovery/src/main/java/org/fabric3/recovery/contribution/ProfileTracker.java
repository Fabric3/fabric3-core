/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
import org.fabric3.host.Namespaces;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeRecover;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Records profiles installed in the runtime.
 *
 * @version $Rev$ $Date$
 */
@Service(names = {ContributionServiceListener.class, Fabric3EventListener.class})
@EagerInit
public class ProfileTracker implements ContributionServiceListener, Fabric3EventListener<RuntimeRecover> {
    private static final QName CONTRIBUTION = new QName(Namespaces.F3, "contribution");
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
        mappings = new HashMap<URI, List<URI>>();
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
            List<URI> deleted = new ArrayList<URI>();
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
        } catch (FileNotFoundException e) {
            monitor.error(e);
        } catch (XMLStreamException e) {
            monitor.error(e);
        } catch (InvalidRepositoryIndexException e) {
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
            writer.writeDefaultNamespace(Namespaces.F3);
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

    private Map<URI, List<URI>> parse() throws FileNotFoundException, XMLStreamException, InvalidRepositoryIndexException {
        Map<URI, List<URI>> profileMappings = new HashMap<URI, List<URI>>();
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
                        List<URI> profiles = new ArrayList<URI>();
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