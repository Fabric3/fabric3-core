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
package org.fabric3.introspection.xml.definitions;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.definitions.IntentMap;
import org.fabric3.model.type.definitions.IntentQualifier;
import org.fabric3.model.type.definitions.PolicyPhase;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loader for definitions.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class PolicySetLoader implements TypeLoader<PolicySet> {

    private final LoaderHelper helper;

    public PolicySetLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public PolicySet load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        Element policyElement = helper.transform(reader).getDocumentElement();

        String name = policyElement.getAttribute("name");
        QName qName = new QName(context.getTargetNamespace(), name);

        Set<QName> provides = new HashSet<QName>();
        StringTokenizer tok = new StringTokenizer(policyElement.getAttribute("provides"));
        while (tok.hasMoreElements()) {
            try {
                provides.add(helper.createQName(tok.nextToken(), reader));
            } catch (InvalidPrefixException e) {
                raiseInvalidPrefix(reader, context, e);
                return null;
            }
        }

        String appliesTo = policyElement.getAttribute("appliesTo");
        String attachTo = policyElement.getAttribute("attachTo");

        Element extension = null;
        Set<IntentMap> intentMaps = new HashSet<IntentMap>();
        Set<QName> policySetReferences = new HashSet<QName>();
        NodeList children = policyElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            String nodeName = node.getNodeName();
            if (node instanceof Element) {
                Element element = (Element) node;
                if ("intentMap".equals(nodeName)) {
                    parseIntentMaps(element, intentMaps, reader, context);
                } else if ("policySetReference".equals(nodeName)) {
                    parsePolicyReference(element, policySetReferences, reader, context);
                } else {
                    // the node is not an intent map or policy set reference, it must be an extension element
                    extension = (Element) children.item(i);
                }
            }
        }

        PolicyPhase phase = parsePhase(extension, reader, context);
        URI uri = context.getContributionUri();
        PolicySet policySet = new PolicySet(qName, provides, appliesTo, attachTo, extension, phase, intentMaps, uri);
        policySet.setPolicySetReferences(policySetReferences);
        validate(policySet, reader, context);
        return policySet;
    }

    /**
     * Parses intent maps in a policy set configuration.
     *
     * @param element    the policy set contents to parse
     * @param intentMaps the intent maps collection to populate
     * @param reader     the StAX reader
     * @param context    the current introspection context
     */
    private void parseIntentMaps(Element element, Set<IntentMap> intentMaps, XMLStreamReader reader, IntrospectionContext context) {
        try {
            QName providedIntent = helper.createQName(element.getAttribute("provides"), reader);
            IntentMap intentMap = new IntentMap(providedIntent);
            if (intentMaps.contains(intentMap)) {
                DuplicateIntentMap error = new DuplicateIntentMap("Duplicate intent map defined for " + providedIntent, reader);
                context.addError(error);
            } else {
                intentMaps.add(intentMap);
            }
            NodeList intentMapQualifiers = element.getElementsByTagName("qualifier");
            for (int n = 0; n < intentMapQualifiers.getLength(); n++) {

                Node qualifierNode = intentMapQualifiers.item(n);
                if (!(qualifierNode instanceof Element)) {
                    continue;
                }
                Element qualifier = (Element) qualifierNode;
                String qualifierName = qualifier.getAttribute("name");
                Element qualifierContents = null;
                NodeList childNodes = qualifier.getChildNodes();
                for (int n2 = 0; n2 < childNodes.getLength(); n2++) {
                    if (childNodes.item(n2) instanceof Element) {
                        qualifierContents = (Element) childNodes.item(n2);
                        break;
                    }
                }
                IntentQualifier intentQualifier = new IntentQualifier(qualifierName, qualifierContents);
                intentMap.addQualifier(intentQualifier);
            }

        } catch (InvalidPrefixException e) {
            raiseInvalidPrefix(reader, context, e);
        }
    }

    /**
     * Parses policy set references in a policy set configuration.
     *
     * @param element             the policy set contents to parse
     * @param policySetReferences the collection of policy set references to update
     * @param reader              the StAX reader
     * @param context             the current introspection context
     */
    private void parsePolicyReference(Element element, Set<QName> policySetReferences, XMLStreamReader reader, IntrospectionContext context) {
        try {
            QName referenceName = helper.createQName(element.getAttribute("name"), reader);
            if (referenceName == null) {
                MissingAttribute error = new MissingAttribute("Policy reference must have a name", reader);
                context.addError(error);
            } else {
                policySetReferences.add(referenceName);
            }
        } catch (InvalidPrefixException e) {
            raiseInvalidPrefix(reader, context, e);
        }
    }

    /**
     * Determines the phase: if the policy language is in the F3 namespace, default to interception phase. Otherwise default to provided phase.
     *
     * @param extension the extension element containing the policy set configuration
     * @param reader    the StAX reader
     * @param context   the introspection context
     * @return the policy phase
     */

    private PolicyPhase parsePhase(Element extension, XMLStreamReader reader, IntrospectionContext context) {
        PolicyPhase phase = PolicyPhase.PROVIDED;
        if (extension != null && Namespaces.F3.equals(extension.getNamespaceURI())) {
            String phaseAttr = extension.getAttributeNS(Namespaces.F3, "phase");
            if (phaseAttr != null && phaseAttr.length() > 0) {
                try {
                    phase = PolicyPhase.valueOf(phaseAttr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    UnrecognizedAttribute failure = new UnrecognizedAttribute("Invalid phase: " + phaseAttr, reader);
                    context.addError(failure);
                    phase = PolicyPhase.INTERCEPTION;
                }

            } else {
                phase = PolicyPhase.INTERCEPTION;
            }
        }
        return phase;
    }

    private void raiseInvalidPrefix(XMLStreamReader reader, IntrospectionContext context, InvalidPrefixException e) {
        String prefix = e.getPrefix();
        URI uri = context.getContributionUri();
        context.addError(new InvalidQNamePrefix("The prefix " + prefix + " specified in the definitions.xml file in contribution " + uri
                + " is invalid", reader));
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"provides".equals(name) && !"appliesTo".equals(name) && !"phase".equals(name) && !"attachTo".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

    private void validate(PolicySet policySet, XMLStreamReader reader, IntrospectionContext context) {
        // validate intent maps
        for (IntentMap intentMap : policySet.getIntentMaps()) {
            if (!policySet.doesProvide(intentMap.getProvides())) {
                InvalidValue error = new InvalidValue("Provides on intent map " + intentMap.getProvides()
                        + " does not match a provides entry on the parent policy set: " + policySet.getName(), reader);
                context.addError(error);
            }
        }

    }


}
    
