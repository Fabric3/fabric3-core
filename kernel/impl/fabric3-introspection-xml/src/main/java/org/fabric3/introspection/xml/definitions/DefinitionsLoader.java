/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.host.contribution.InstallException;
import org.fabric3.api.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.api.model.type.definitions.BindingType;
import org.fabric3.api.model.type.definitions.ExternalAttachment;
import org.fabric3.api.model.type.definitions.ImplementationType;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.IntentType;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.api.model.type.definitions.Qualifier;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.Symbol;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoader;
import org.fabric3.spi.contribution.xml.XmlResourceElementLoaderRegistry;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Reference;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.oasisopen.sca.Constants.SCA_NS;

/**
 * Loader for policy definitions.
 */
@EagerInit
public class DefinitionsLoader implements XmlResourceElementLoader {

    static final QName INTENT = new QName(SCA_NS, "intent");
    static final QName POLICY_SET = new QName(SCA_NS, "policySet");
    static final QName BINDING_TYPE = new QName(SCA_NS, "bindingType");
    static final QName IMPLEMENTATION_TYPE = new QName(SCA_NS, "implementationType");
    static final QName DEFINITIONS = new QName(SCA_NS, "definitions");
    static final QName EXTERNAL_ATTACHMENT = new QName(SCA_NS, "externalAttachment");

    private XmlResourceElementLoaderRegistry elementLoaderRegistry;
    private Loader loaderRegistry;

    public DefinitionsLoader(@Reference XmlResourceElementLoaderRegistry elementLoaderRegistry, @Reference Loader loader) {
        this.elementLoaderRegistry = elementLoaderRegistry;
        this.loaderRegistry = loader;
    }

    @Init
    public void init() {
        elementLoaderRegistry.register(this);
    }

    public QName getType() {
        return DEFINITIONS;
    }

    public void load(XMLStreamReader reader, Resource resource, IntrospectionContext context) throws InstallException, XMLStreamException {

        validateAttributes(reader, context);
        List<AbstractPolicyDefinition> definitions = new ArrayList<AbstractPolicyDefinition>();
        String oldNamespace = context.getTargetNamespace();
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
        context.setTargetNamespace(targetNamespace);
        while (true) {
            switch (reader.next()) {
                case START_ELEMENT:
                    Location location = reader.getLocation();
                    QName qname = reader.getName();
                    AbstractPolicyDefinition definition = null;
                    if (INTENT.equals(qname)) {
                        definition = loaderRegistry.load(reader, Intent.class, context);
                    } else if (POLICY_SET.equals(qname)) {
                        definition = loaderRegistry.load(reader, PolicySet.class, context);
                    } else if (BINDING_TYPE.equals(qname)) {
                        definition = loaderRegistry.load(reader, BindingType.class, context);
                    } else if (IMPLEMENTATION_TYPE.equals(qname)) {
                        definition = loaderRegistry.load(reader, ImplementationType.class, context);
                    } else if (EXTERNAL_ATTACHMENT.equals(qname)) {
                        definition = loaderRegistry.load(reader, ExternalAttachment.class, context);
                    } else {
                        UnrecognizedElement failure = new UnrecognizedElement(reader, location);
                        context.addError(failure);
                    }
                    if (definition != null) {
                        if (definitions.contains(definition)) {
                            QName name = definition.getName();
                            DuplicatePolicyDefinition error = new DuplicatePolicyDefinition("Duplicate policy definition: " + name, location);
                            context.addError(error);
                        }
                        definitions.add(definition);
                    }
                    break;
                case END_ELEMENT:
                    // update indexed elements with the loaded definitions
                    for (AbstractPolicyDefinition candidate : definitions) {
                        if (candidate instanceof ExternalAttachment) {
                            // External attachments must be added here and not during indexing since they have synthetic names
                            QNameSymbol symbol = new QNameSymbol(candidate.getName());
                            ResourceElement<QNameSymbol, AbstractPolicyDefinition> element = new ResourceElement<QNameSymbol, AbstractPolicyDefinition>(symbol);
                            element.setValue(candidate);
                            resource.addResourceElement(element);

                            continue;
                        }
                        boolean found = false;
                        for (ResourceElement element : resource.getResourceElements()) {
                            Symbol candidateSymbol = new QNameSymbol(candidate.getName());
                            if (element.getSymbol().equals(candidateSymbol)) {
                                //noinspection unchecked
                                element.setValue(candidate);
                                found = true;
                            }
                        }
                        if (!found) {
                            String id = candidate.toString();
                            throw new AssertionError("Definition not found: " + id);
                        }
                        if (candidate instanceof Intent) {
                            expandQualifiers((Intent) candidate, resource);
                        }
                    }
                    context.setTargetNamespace(oldNamespace);
                    return;
            }
        }

    }

    /**
     * Creates Intent instances from qualifier entries defined in an unqualified (parent) intent.
     *
     * @param intent   the unqualified intent
     * @param resource the contribution resource containing the intents
     */
    private void expandQualifiers(Intent intent, Resource resource) {
        String ns = intent.getName().getNamespaceURI();
        String localPart = intent.getName().getLocalPart();
        for (Qualifier qualifier : intent.getQualifiers()) {
            QName qualifierName = new QName(ns, localPart + "." + qualifier.getName());
            QName constrains = intent.getConstrains();
            Set<QName> requires = intent.getRequires();
            IntentType intentType = intent.getIntentType();
            boolean isDefault = qualifier.isDefault();
            Set<QName> excludes = new HashSet<QName>(intent.getExcludes());
            if (intent.isMutuallyExclusive()) {
                // for each qualified intent, add the others as excludes
                for (Qualifier entry : intent.getQualifiers()) {
                    if (entry == qualifier) {
                        continue;  // skip self
                    }
                    excludes.add(new QName(ns, localPart + "." + entry.getName()));
                }
            }
            Intent qualified = new Intent(qualifierName, constrains, requires, Collections.<Qualifier>emptySet(), false, excludes, intentType, isDefault);
            QNameSymbol symbol = new QNameSymbol(qualifierName);
            ResourceElement<QNameSymbol, AbstractPolicyDefinition> element = new ResourceElement<QNameSymbol, AbstractPolicyDefinition>(symbol);
            element.setValue(qualified);
            resource.addResourceElement(element);
        }
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        Location location = reader.getLocation();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"targetNamespace".equals(name)) {
                UnrecognizedAttribute failure = new UnrecognizedAttribute(name, location);
                context.addError(failure);
            }
        }
    }

}
