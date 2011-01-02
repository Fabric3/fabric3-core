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
package org.fabric3.introspection.xml.definitions;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.definitions.AbstractPolicyDefinition;
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
 *
 * @version $Rev$ $Date$
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
                    QName qname = reader.getName();
                    if (!INTENT.equals(qname)
                            && !POLICY_SET.equals(qname)
                            && !BINDING_TYPE.equals(qname)
                            && !IMPLEMENTATION_TYPE.equals(qname)) {
                        continue;
                    }
                    String nameAttr = reader.getAttributeValue(null, "name");
                    if (nameAttr == null) {
                        context.addError(new MissingAttribute("Definition name not specified", reader));
                        return;
                    }
                    NamespaceContext namespaceContext = reader.getNamespaceContext();
                    QName name = LoaderUtil.getQName(nameAttr, targetNamespace, namespaceContext);
                    QNameSymbol symbol = new QNameSymbol(name);
                    ResourceElement<QNameSymbol, AbstractPolicyDefinition> element = new ResourceElement<QNameSymbol, AbstractPolicyDefinition>(symbol);
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