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
package org.fabric3.introspection.xml.definitions;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.xml.XmlIndexer;
import org.fabric3.spi.contribution.xml.XmlIndexerRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Indexer for definitions.
 */
@EagerInit
public class DefinitionsIndexer implements XmlIndexer {
    private static final QName DEFINITIONS = new QName(SCA_NS, "definitions");
    private static final QName INTENT = new QName(SCA_NS, "intent");
    private static final QName POLICY_SET = new QName(SCA_NS, "policySet");
    private static final QName BINDING_TYPE = new QName(SCA_NS, "bindingType");
    private static final QName IMPLEMENTATION_TYPE = new QName(SCA_NS, "implementationType");
    private XmlIndexerRegistry registry;

    public DefinitionsIndexer(@Reference XmlIndexerRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public QName getType() {
        return DEFINITIONS;
    }

    public void index(Resource resource, XMLStreamReader reader, IntrospectionContext context) throws InstallException {
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");

        while (true) {
            try {
                switch (reader.next()) {
                    case START_ELEMENT:
                        Location location = reader.getLocation();

                        QName qname = reader.getName();
                        if (!INTENT.equals(qname) && !POLICY_SET.equals(qname) && !BINDING_TYPE.equals(qname) && !IMPLEMENTATION_TYPE.equals(qname)) {
                            continue;
                        }
                        String nameAttr = reader.getAttributeValue(null, "name");

                        if (nameAttr == null) {
                            // support old SCA and SCA 1.1 attributes for backward compatibility
                            nameAttr = reader.getAttributeValue(null, "type");
                            if (nameAttr == null) {
                                context.addError(new MissingAttribute("Definition name not specified", location));
                                return;
                            }
                        }
                        NamespaceContext namespaceContext = reader.getNamespaceContext();
                        QName name = LoaderUtil.getQName(nameAttr, targetNamespace, namespaceContext);
                        QNameSymbol symbol = new QNameSymbol(name);
                        ResourceElement<QNameSymbol, AbstractPolicyDefinition> element = new ResourceElement<>(symbol);
                        resource.addResourceElement(element);
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        return;
                }
            } catch (XMLStreamException e) {
                throw new InstallException(e);
            }
        }

    }

}