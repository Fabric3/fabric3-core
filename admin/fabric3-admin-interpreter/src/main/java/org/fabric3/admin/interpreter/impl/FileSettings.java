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
package org.fabric3.admin.interpreter.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.admin.interpreter.DomainConfiguration;
import org.fabric3.admin.interpreter.Settings;

/**
 * An implementation that stores settings to a properties file.
 */
public class FileSettings implements Settings {
    private File file;
    private Map<String, DomainConfiguration> domains = new HashMap<>();

    public FileSettings(File file) {
        this.file = file;
    }

    public void addConfiguration(DomainConfiguration configuration) {
        domains.put(configuration.getName(), configuration);
    }

    public DomainConfiguration getDomainConfiguration(String name) {
        return domains.get(name);
    }

    public List<DomainConfiguration> getDomainConfigurations() {
        return new ArrayList<>(domains.values());
    }

    public void save() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void load() throws IOException {

        if (!file.exists()) {
            return;
        }
        InputStream stream = null;
        XMLStreamReader reader = null;
        try {
            domains.clear();
            stream = new FileInputStream(file);
            try {
                reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
                while (true) {
                    int val = reader.next();
                    switch (val) {
                    case (XMLStreamConstants.START_ELEMENT):
                        if ("domain".equals(reader.getLocalName())) {
                            String name = reader.getAttributeValue(null, "name");
                            String url = reader.getAttributeValue(null, "url");
                            String username = reader.getAttributeValue(null, "username");
                            String password = reader.getAttributeValue(null, "password");
                            DomainConfiguration configuration = new DomainConfiguration(name, url, username, password);
                            addConfiguration(configuration);
                            break;
                        }
                        break;
                    case (XMLStreamConstants.END_DOCUMENT):
                        return;
                    }
                }
            } catch (XMLStreamException e) {
                throw new IOException(e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    e.printStackTrace();
                }
            }
            if (stream != null) {
                stream.close();
            }
        }
    }
}
