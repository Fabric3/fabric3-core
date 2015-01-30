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
package org.fabric3.cache.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.cache.model.CacheSetResource;
import org.fabric3.cache.spi.CacheResource;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 * Loads cache configurations specified in a composite. The format of the caches element is:
 * <pre>
 *      &lt;caches&gt;
 *          &lt;cache name="MyCache"&gt;
 *              &lt;!-- cache-specific configuration --&gt
 *           &lt;/cache&gt;
 *      &lt;/caches&gt;
 * </pre>
 */
@EagerInit
public class CacheResourceLoader extends AbstractValidatingTypeLoader<CacheSetResource> {
    private static final QName SCA_TYPE = new QName(Constants.SCA_NS, "caches");
    private static final QName F3_TYPE = new QName(org.fabric3.api.Namespaces.F3, "caches");

    private LoaderRegistry registry;

    public CacheResourceLoader(@Reference LoaderRegistry registry) {
        this.registry = registry;
        addAttributes("name");
    }

    @Init
    public void init() {
        // register under both namespaces
        registry.registerLoader(F3_TYPE, this);
        registry.registerLoader(SCA_TYPE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(F3_TYPE);
        registry.unregisterLoader(SCA_TYPE);
    }


    public CacheSetResource load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        CacheSetResource cacheSetResource = new CacheSetResource();

        validateAttributes(reader, context, cacheSetResource);

        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("cache".equals(reader.getName().getLocalPart())) {
                    Location location = reader.getLocation();
                    String name = reader.getAttributeValue(null, "name");

                    if (null == name) {
                        MissingAttribute error = new MissingAttribute("Cache name not specified", location);
                        context.addError(error);
                        name = "default";
                    }

                    reader.nextTag();
                    CacheResource configuration = registry.load(reader, CacheResource.class, context);
                    if (configuration == null) {
                        continue;
                    }
                    configuration.setCacheName(name);
                    cacheSetResource.addDefinition(configuration);
                }
                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("caches".equals(reader.getName().getLocalPart())) {
                    return cacheSetResource;
                }
            }
        }
    }

}