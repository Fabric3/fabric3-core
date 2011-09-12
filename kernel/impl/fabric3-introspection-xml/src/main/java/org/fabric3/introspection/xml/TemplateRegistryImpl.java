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
package org.fabric3.introspection.xml;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.Service;

import org.fabric3.model.type.ModelObject;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.introspection.xml.DuplicateTemplateException;
import org.fabric3.spi.introspection.xml.TemplateRegistry;

/**
 * Default implementation of the {@link TemplateRegistry}. Also implements {@link ContributionServiceListener} to unregister templates when their
 * containing contribution is undeployed.
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = TemplateRegistry.class)
public class TemplateRegistryImpl implements TemplateRegistry, ContributionServiceListener {
    private Map<String, Pair> cache = new ConcurrentHashMap<String, Pair>();

    public <T extends ModelObject> void register(String name, URI uri, T value) throws DuplicateTemplateException {
        if (cache.containsKey(name)) {
            throw new DuplicateTemplateException(name);
        }
        Pair pair = new Pair(uri, value);
        cache.put(name, pair);
    }

    public void unregister(String name) {
        cache.remove(name);
    }

    public <T extends ModelObject> T resolve(Class<T> type, String name) {
        Pair pair = cache.get(name);
        if (pair != null) {
            return type.cast(pair.object);
        }
        return null;
    }

    public void onUninstall(Contribution contribution) {
        URI uri = contribution.getUri();
        for (Iterator<Pair> iterator = cache.values().iterator(); iterator.hasNext();) {
            Pair pair = iterator.next();
            if (pair.contributionUri.equals(uri)) {
                iterator.remove();
            }
        }
    }

    public void onRemove(Contribution contribution) {
        // no-op
    }

    public void onStore(Contribution contribution) {
        // no-op
    }

    public void onProcessManifest(Contribution contribution) {
        // no-op
    }

    public void onInstall(Contribution contribution) {
        // no-op
    }

    public void onUpdate(Contribution contribution) {
        // no-op
    }

    private class Pair {
        private URI contributionUri;
        private ModelObject object;

        private Pair(URI contributionUri, ModelObject object) {
            this.contributionUri = contributionUri;
            this.object = object;
        }

    }
}
