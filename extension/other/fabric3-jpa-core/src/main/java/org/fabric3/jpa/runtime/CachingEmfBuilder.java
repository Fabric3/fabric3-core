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
package org.fabric3.jpa.runtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.jpa.spi.EmfBuilderException;
import org.fabric3.jpa.spi.delegate.EmfBuilderDelegate;
import org.fabric3.spi.builder.classloader.ClassLoaderListener;

/**
 * Creates entity manager factories using the JPA provider SPI. Creation of entity manager factories are expensive operations and hence created
 * instances are cached.
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = {EmfBuilder.class, EmfCache.class, ClassLoaderListener.class})
public class CachingEmfBuilder implements EmfBuilder, EmfCache, ClassLoaderListener {
    private PersistenceUnitScanner scanner;
    private Map<String, EntityManagerFactory> cache = new HashMap<String, EntityManagerFactory>();
    private Map<String, EmfBuilderDelegate> delegates = new HashMap<String, EmfBuilderDelegate>();
    private Map<ClassLoader, Set<String>> classLoaderCache = new HashMap<ClassLoader, Set<String>>();

    /**
     * Constructor.
     *
     * @param scanner the scanner used to resolve persistence unit definitions.
     */
    public CachingEmfBuilder(@Reference PersistenceUnitScanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Injects the delegates.
     *
     * @param delegates Provider specific delegates.
     */
    @Reference(required = false)
    public void setDelegates(Map<String, EmfBuilderDelegate> delegates) {
        this.delegates = delegates;
    }

    public void onBuild(ClassLoader loader) {
        // no-op
    }

    public void onDestroy(ClassLoader loader) {
        Set<String> names = classLoaderCache.remove(loader);
        if (names != null) {
            for (String name : names) {
                cache.remove(name);
                scanner.release(name);
            }
        }
    }

    public synchronized EntityManagerFactory build(String unitName, ClassLoader classLoader) throws EmfBuilderException {
        EntityManagerFactory emf = cache.get(unitName);
        if (emf != null) {
            return emf;
        }
        emf = createEntityManagerFactory(unitName, classLoader);
        cache.put(unitName, emf);
        Set<String> names = classLoaderCache.get(classLoader);
        if (names == null) {
            names = new HashSet<String>();
            classLoaderCache.put(classLoader, names);
        }
        names.add(unitName);
        return emf;
    }

    /**
     * Closes the entity manager factories.
     */
    @Destroy
    public void destroy() {
        for (EntityManagerFactory emf : cache.values()) {
            if (emf != null) {
                emf.close();
            }
        }
    }

    public EntityManagerFactory getEmf(String unitName) {
        return cache.get(unitName);
    }

    /**
     * Creates the entity manager factory using the JPA provider API.
     *
     * @param unitName    the persistence unit name
     * @param classLoader the persistence unit classloader
     * @return the entity manager factory
     * @throws EmfBuilderException if there is an error creating the factory
     */
    private EntityManagerFactory createEntityManagerFactory(String unitName, ClassLoader classLoader) throws EmfBuilderException {
        F3PersistenceUnitInfo info = scanner.getPersistenceUnitInfo(unitName, classLoader);
        String providerClass = info.getPersistenceProviderClassName();
        String dataSourceName = info.getDataSourceName();

        EmfBuilderDelegate delegate = null;
        if (providerClass != null) {
            delegate = delegates.get(providerClass);
        } else if (!delegates.isEmpty()) {
            // no provider specified, get the first one
            delegate = delegates.values().iterator().next();
        }
        if (delegate != null) {
            return delegate.build(info, classLoader, dataSourceName);
        }

        // No configured delegates, try standard JPA
        try {
            PersistenceProvider provider = (PersistenceProvider) classLoader.loadClass(providerClass).newInstance();
            return provider.createContainerEntityManagerFactory(info, Collections.emptyMap());
        } catch (InstantiationException ex) {
            throw new EmfBuilderException(ex);
        } catch (IllegalAccessException ex) {
            throw new EmfBuilderException(ex);
        } catch (ClassNotFoundException ex) {
            throw new EmfBuilderException(ex);
        }

    }

}
