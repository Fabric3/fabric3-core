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
package org.fabric3.security.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.Role;
import org.fabric3.host.contribution.StoreException;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.security.BasicSecuritySubject;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Reads security information from a system property or the <code>security.xml</code> file in the runtime config directory.
 *
 * @version $Rev$ $Date$
 */
public class FileSecurityStore implements SecurityStore {
    private XMLInputFactory xmlFactory;
    private HostInfo info;
    private Map<String, BasicSecuritySubject> cache;

    public FileSecurityStore(@Reference XMLFactory xmlFactory, @Reference HostInfo info) {
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
        this.info = info;
    }

    @Property(required = false)
    public void setSecurityConfiguration(XMLStreamReader reader) throws XMLStreamException, StoreException {
        cache = new ConcurrentHashMap<String, BasicSecuritySubject>();
        parse(reader);
    }

    @Init
    public void init() throws FileNotFoundException, XMLStreamException, StoreException {
        if (cache != null) {
            // initialized from a system property
            return;
        }
        cache = new ConcurrentHashMap<String, BasicSecuritySubject>();
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

    public BasicSecuritySubject find(String username) throws SecurityStoreException {
        return cache.get(username);
    }

    private void parse(XMLStreamReader reader) throws XMLStreamException, StoreException {
        reader.nextTag();
        String username = null;
        String password = null;
        Set<Role> roles = new HashSet<Role>();
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
                    roles = new HashSet<Role>();
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
