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
package org.fabric3.jpa.runtime.emf;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManagerFactory;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Service;

import org.fabric3.host.Names;
import org.fabric3.spi.builder.classloader.ClassLoaderListener;
import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * Creates and caches entity manager factories.
 *
 * @version $Rev: 8837 $ $Date: 2010-04-08 14:05:46 +0200 (Thu, 08 Apr 2010) $
 */
@Service(interfaces = {EmfCache.class, ClassLoaderListener.class})
public class DefaultEmfCache implements EmfCache, ClassLoaderListener {
    private Map<String, EntityManagerFactory> cache = new HashMap<String, EntityManagerFactory>();
    private Map<URI, Set<String>> contributionCache = new HashMap<URI, Set<String>>();

    @Destroy
    public void destroy() {
        // the runtime is being shutdown, close any open factories
        for (EntityManagerFactory emf : cache.values()) {
            if (emf != null) {
                emf.close();
            }
        }
    }

    public void onDeploy(ClassLoader loader) {
        // no-op
    }

    public void onUndeploy(ClassLoader classLoader) {
        URI key;
        if (classLoader instanceof MultiParentClassLoader) {
            key = ((MultiParentClassLoader) classLoader).getName();
        } else {
            key = Names.BOOT_CONTRIBUTION;
        }
        Set<String> names = contributionCache.remove(key);
        if (names != null) {
            for (String name : names) {
                EntityManagerFactory emf = cache.remove(name);
                emf.close();
            }
        }
    }

    public EntityManagerFactory getEmf(String unitName) {
        return cache.get(unitName);
    }

    public void putEmf(URI uri, String unitName, EntityManagerFactory emf) {
        cache.put(unitName, emf);
        Set<String> names = contributionCache.get(uri);
        if (names == null) {
            names = new HashSet<String>();
            contributionCache.put(uri, names);
        }
        names.add(unitName);
    }

}