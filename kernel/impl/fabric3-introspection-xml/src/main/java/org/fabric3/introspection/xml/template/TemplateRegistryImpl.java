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
 */
package org.fabric3.introspection.xml.template;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.annotation.Service;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionServiceListener;
import org.fabric3.spi.introspection.xml.DuplicateTemplateException;
import org.fabric3.spi.introspection.xml.TemplateRegistry;

/**
 * Default implementation of the {@link TemplateRegistry}. Also implements {@link ContributionServiceListener} to unregister templates when their
 * containing contribution is undeployed.
 */
@Service(TemplateRegistry.class)
public class TemplateRegistryImpl implements TemplateRegistry, ContributionServiceListener {
    private Map<String, Pair> cache = new ConcurrentHashMap<>();

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
