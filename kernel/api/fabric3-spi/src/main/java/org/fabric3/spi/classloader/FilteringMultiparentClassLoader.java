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
package org.fabric3.spi.classloader;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A ClassLoader implementations that only loads classes matching a set of regular expression patterns from its parent. For example,
 * <code>org.fabric3.test.*</code> loads all classes in the <code>org.fabric3.test</code> package and its subpackages.
 *
 * @version $Rev$ $Date$
 */
public class FilteringMultiparentClassLoader extends MultiParentClassLoader {
    private Set<Pattern> patterns = new HashSet<Pattern>();

    public FilteringMultiparentClassLoader(URI name, ClassLoader parent, Set<String> filters) {
        super(name, parent);
        compile(filters);
    }

    public FilteringMultiparentClassLoader(URI name, URL[] urls, ClassLoader parent, Set<String> filters) {
        super(name, urls, parent);
        compile(filters);
    }

    public Set<Pattern> getPatterns() {
        return patterns;
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(name);
        if (clazz == null) {
            boolean found = false;
            for (Pattern pattern : patterns) {
                String replacedName = name.replace(".", "\\.");
                if (pattern.matcher(replacedName).matches()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // check in the current classloader's classpath
                clazz = findClass(name);
            }
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);
                return clazz;
            }
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    private void compile(Set<String> filters) {
        for (String filter : filters) {
            String replacedFilter = filter.replace(".", "..");
            Pattern p = Pattern.compile(replacedFilter);
            patterns.add(p);
        }
    }

}
