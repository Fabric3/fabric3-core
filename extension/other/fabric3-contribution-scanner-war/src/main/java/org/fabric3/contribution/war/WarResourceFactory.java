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
package org.fabric3.contribution.war;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.contribution.scanner.spi.FileResource;
import org.fabric3.contribution.scanner.spi.FileSystemResource;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactory;
import org.fabric3.contribution.scanner.spi.FileSystemResourceFactoryRegistry;

/**
 * Creates a FileResource for SCA contribution jars
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WarResourceFactory implements FileSystemResourceFactory {

    public WarResourceFactory(@Reference FileSystemResourceFactoryRegistry registry) {
        registry.register(this);
    }

    public FileSystemResource createResource(File file) {
        if (!file.getName().endsWith(".war")) {
            return null;
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file.getCanonicalPath());
            JarEntry entry = jarFile.getJarEntry("WEB-INF/sca-contribution.xml");
            if (entry == null) {
                return null;
            }
        } catch (FileNotFoundException e) {
            // no sca-contribution, ignore
            return null;
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return new FileResource(file);
    }
}