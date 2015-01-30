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
 */
package org.fabric3.security.impl;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.Role;
import org.fabric3.api.host.contribution.StoreException;
import org.fabric3.api.host.runtime.HostInfo;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;

/**
 * Reads security information from a system property or the <code>security.xml</code> file in the runtime config directory.
 */
public class FileSecurityStore implements SecurityStore {
    private XMLInputFactory xmlFactory;
    private HostInfo info;
    private Map<String, BasicSecuritySubject> cache;

    public FileSecurityStore(@Reference HostInfo info) {
        this.xmlFactory = XMLInputFactory.newFactory();
        this.info = info;
    }

    @Property(required = false)
    public void setSecurityConfiguration(XMLStreamReader reader) throws XMLStreamException, StoreException {
        cache = new ConcurrentHashMap<>();
        parse(reader);
    }

    @Init
    public void init() throws FileNotFoundException, XMLStreamException, StoreException {
        if (cache != null) {
            // initialized from a system property
            return;
        }
        cache = new ConcurrentHashMap<>();
        File dir = info.getBaseDir();
        if (dir == null) {
            // runtime does not have a filesystem image
            return;
        }
        File securityFile = new File(dir, "config" + File.separator + "security.xml");
        if (!securityFile.exists()) {
            return;
        }
        FileInputStream fis = new FileInputStream(securityFile);
        BufferedInputStream buffered = new BufferedInputStream(fis);
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(buffered);
        parse(reader);
    }

    public BasicSecuritySubject find(String username) {
        return cache.get(username);
    }

    private void parse(XMLStreamReader reader) throws XMLStreamException, StoreException {
        reader.nextTag();
        String username = null;
        String password = null;
        Set<Role> roles = new HashSet<>();
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("username".equals(reader.getName().getLocalPart())) {
                    username = reader.getElementText();
                } else if ("password".equals(reader.getName().getLocalPart())) {
                    password = reader.getElementText();
                } else if ("roles".equals(reader.getName().getLocalPart())) {
                    roles = parseRoles(reader, roles);
                }

                break;
            case XMLStreamConstants.END_DOCUMENT:
                return;
            case XMLStreamConstants.END_ELEMENT:
                if ("user".equals(reader.getName().getLocalPart())) {
                    if (username == null) {
                        raiseInvalidConfiguration("Username is missing", reader);
                    }
                    if (password == null) {
                        raiseInvalidConfiguration("Password is missing", reader);
                    }
                    BasicSecuritySubject subject = new BasicSecuritySubject(username, password, roles);
                    cache.put(subject.getUsername(), subject);
                    username = null;
                    password = null;
                    roles = new HashSet<>();
                }
            }
        }

    }

    private Set<Role> parseRoles(XMLStreamReader reader, Set<Role> roles) throws XMLStreamException, StoreException {
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("role".equals(reader.getName().getLocalPart())) {
                    Role role = new Role(reader.getElementText());
                    roles.add(role);
                }

                break;
            case XMLStreamConstants.END_DOCUMENT:
                raiseInvalidConfiguration("Invalid end of document", reader);
            case XMLStreamConstants.END_ELEMENT:
                if ("roles".equals(reader.getName().getLocalPart())) {
                    return roles;
                }
            }
        }
    }


    private void raiseInvalidConfiguration(String message, XMLStreamReader reader) throws StoreException {
        Location location = reader.getLocation();
        if (location == null) {
            throw new StoreException(message);
        }
        int line = location.getLineNumber();
        int col = location.getColumnNumber();
        throw new StoreException(message + " [" + line + "," + col + "]");
    }

}
