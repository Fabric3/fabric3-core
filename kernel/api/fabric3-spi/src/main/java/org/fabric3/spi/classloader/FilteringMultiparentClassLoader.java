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
package org.fabric3.spi.classloader;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A ClassLoader implementations that only loads classes matching a set of regular expression patterns from its parent. For example,
 * <code>org.fabric3.test.*</code> loads all classes in the <code>org.fabric3.test</code> package and its subpackages.
 */
public class FilteringMultiparentClassLoader extends MultiParentClassLoader {
    private Set<Pattern> patterns = new HashSet<>();

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
