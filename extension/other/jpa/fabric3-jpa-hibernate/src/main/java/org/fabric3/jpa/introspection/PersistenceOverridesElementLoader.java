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
package org.fabric3.jpa.introspection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.api.model.type.ModelObject;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

/**
 * Loads a <code>&lt;persistence&gt;</code> element in a composite.
 */
@EagerInit
public class PersistenceOverridesElementLoader implements TypeLoader<ModelObject> {
    private static final QName QNAME = new QName(org.fabric3.api.Namespaces.F3, "persistence");
    private LoaderRegistry loaderRegistry;

    public PersistenceOverridesElementLoader(@Reference LoaderRegistry loaderRegistry) {
        this.loaderRegistry = loaderRegistry;
    }

    @Init
    public void init() {
        loaderRegistry.registerLoader(QNAME, this);
    }

    @Destroy
    public void destroy() {
        loaderRegistry.unregisterLoader(QNAME);
    }

    public ModelObject load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                loaderRegistry.load(reader, ModelObject.class, context);
                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("persistence".equals(reader.getName().getLocalPart())) {
                    return null;
                }
            }
        }
    }
}
