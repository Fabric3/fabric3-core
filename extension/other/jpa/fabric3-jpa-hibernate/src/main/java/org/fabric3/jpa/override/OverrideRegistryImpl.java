/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.jpa.override;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.annotation.Service;

import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;

/**
 *
 */
@Service(OverrideRegistry.class)
public class OverrideRegistryImpl implements OverrideRegistry, ContributionServiceListener {
    private Map<URI, List<PersistenceOverrides>> index = new ConcurrentHashMap<URI, List<PersistenceOverrides>>();
    private Map<String, PersistenceOverrides> cache = new ConcurrentHashMap<String, PersistenceOverrides>();

    public void register(URI contributionURI, PersistenceOverrides overrides) throws DuplicateOverridesException {
        String unitName = overrides.getUnitName();
        if (cache.containsKey(unitName)) {
            throw new DuplicateOverridesException(unitName);
        }
        List<PersistenceOverrides> list = index.get(contributionURI);
        if (list == null) {
            list = new ArrayList<PersistenceOverrides>();
            index.put(contributionURI, list);
        }
        list.add(overrides);
        cache.put(unitName, overrides);
    }

    public PersistenceOverrides resolve(String unitName) {
        return cache.get(unitName);
    }

    public void onUninstall(Contribution contribution) {
        List<PersistenceOverrides> overrides = index.remove(contribution.getUri());
        if (overrides != null) {
            for (PersistenceOverrides override : overrides) {
                cache.remove(override.getUnitName());
            }
        }
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

    public void onRemove(Contribution contribution) {
        // no-op
    }
}