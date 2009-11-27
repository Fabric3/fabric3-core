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
package org.fabric3.admin.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fabric3.admin.interpreter.Settings;

/**
 * An implementation that stores settings to a properties file.
 *
 * @version $Rev$ $Date$
 */
public class FileSettings implements Settings {
    private File file;
    private Properties domains = new Properties();

    public FileSettings(File file) {
        this.file = file;
    }

    public void addDomain(String name, String address) {
        domains.put(name, address);
    }

    public String getDomainAddress(String name) {
        return (String) domains.get(name);
    }

    public Map<String, String> getDomainAddresses() {
        Map<String, String> addresses = new HashMap<String, String>(domains.size());
        for (Map.Entry<Object, Object> entry : domains.entrySet()) {
            addresses.put((String) entry.getKey(), (String) entry.getValue());
        }
        return addresses;
    }

    public void save() throws IOException {
        OutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            domains.store(stream, "F3 domain configuration");
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public void load() throws IOException {
        if (!file.exists()) {
            return;
        }
        InputStream stream = null;
        try {
            domains.clear();
            stream = new FileInputStream(file);
            domains.load(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
