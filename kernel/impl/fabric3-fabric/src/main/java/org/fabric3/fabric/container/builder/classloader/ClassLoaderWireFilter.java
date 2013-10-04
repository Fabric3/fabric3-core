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
package org.fabric3.fabric.container.builder.classloader;

import java.net.URL;

import org.fabric3.host.classloader.DelegatingResourceClassLoader;

/**
 * A bridging classloader that filters class and resource loading to a specified set of classes. This is used to enforce the semantics of a
 * JavaContributionWire.
 */
public class ClassLoaderWireFilter extends DelegatingResourceClassLoader {
    private static final URL[] NO_URLS = new URL[0];
    private String[] importedPackage;

    /**
     * Constructor.
     *
     * @param parent          the parent classloader.
     * @param importedPackage the package the wire imports
     */
    public ClassLoaderWireFilter(ClassLoader parent, String importedPackage) {
        super(NO_URLS, parent);
        this.importedPackage = importedPackage.split("\\.");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        String[] clazz = name.split("\\.");
        for (int i = 0; i < importedPackage.length; i++) {
            String packageName = importedPackage[i];
            if ("*".equals(packageName)) {
                // wildcard reached, packages match
                break;
            } else if (clazz.length - 1 >= i && !packageName.equals(clazz[i])) {
                throw new ClassNotFoundException(name);
            }

        }
        return super.loadClass(name, resolve);
    }
}
