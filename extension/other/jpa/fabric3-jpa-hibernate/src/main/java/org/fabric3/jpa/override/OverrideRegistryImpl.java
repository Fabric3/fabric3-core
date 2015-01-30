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
package org.fabric3.jpa.override;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.jpa.api.PersistenceOverrides;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.oasisopen.sca.annotation.Service;

/**
 *
 */
@Service(OverrideRegistry.class)
public class OverrideRegistryImpl implements OverrideRegistry, ContributionServiceListener {
    private Map<URI, List<PersistenceOverrides>> index = new ConcurrentHashMap<>();
    private Map<String, PersistenceOverrides> cache = new ConcurrentHashMap<>();

    public void register(URI contributionURI, PersistenceOverrides overrides) throws DuplicateOverridesException {
        String unitName = overrides.getUnitName();
        if (cache.containsKey(unitName)) {
            throw new DuplicateOverridesException(unitName);
        }
        List<PersistenceOverrides> list = index.get(contributionURI);
        if (list == null) {
            list = new ArrayList<>();
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