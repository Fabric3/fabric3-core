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
package org.fabric3.introspection.xml.definitions;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.oasisopen.sca.Constants;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.IntentType;
import org.fabric3.model.type.definitions.Qualifier;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.AbstractValidatingTypeLoader;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

/**
 * Loader for definitions.
 */
public class IntentLoader extends AbstractValidatingTypeLoader<Intent> {
    private static final QName QUALIFIER = new QName(Constants.SCA_NS, "qualifier");

    private LoaderHelper helper;

    public IntentLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
        addAttributes("name", "constrains", "requires", "excludes", "intentType", "appliesTo", "mutuallyExclusive");
    }

    public Intent load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        Location startLocation = reader.getLocation();
        String name = reader.getAttributeValue(null, "name");
        QName qName = LoaderUtil.getQName(name, context.getTargetNamespace(), reader.getNamespaceContext());

        if (name != null && name.contains(".")) {
            InvalidValue error = new InvalidValue("Profile intent names cannot contain a '.':" + qName, startLocation);
            context.addError(error);
        }

        String constrainsVal = reader.getAttributeValue(null, "constrains");
        QName constrains = null;
        if (constrainsVal != null) {
            try {
                constrains = helper.createQName(constrainsVal, reader);
            } catch (InvalidPrefixException e) {
                String prefix = e.getPrefix();
                URI uri = context.getContributionUri();
                InvalidQNamePrefix failure =
                        new InvalidQNamePrefix("The prefix " + prefix + " specified in the definitions.xml file in contribution " + uri
                                                       + " is invalid", startLocation);
                context.addError(failure);
                return null;
            }
        }
        IntentType intentType = IntentType.INTERACTION;
        String intentTypeVal = reader.getAttributeValue(null, "intentType");
        if (intentTypeVal != null) {
            try {
                intentType = IntentType.valueOf(intentTypeVal.toUpperCase());
            } catch (IllegalArgumentException e) {
                UnrecognizedAttribute failure = new UnrecognizedAttribute("Unknown intentType value: " + intentTypeVal, startLocation);
                context.addError(failure);
                return null;
            }
        }
        Set<QName> requires;
        try {
            requires = helper.parseListOfQNames(reader, "requires");
        } catch (InvalidPrefixException e) {
            String prefix = e.getPrefix();
            URI uri = context.getContributionUri();
            InvalidQNamePrefix failure =
                    new InvalidQNamePrefix("The requires prefix " + prefix + " specified in the definitions.xml file in contribution " + uri
                                                   + " is invalid", startLocation);
            context.addError(failure);
            return null;
        }

        boolean mutuallyExclusive = Boolean.parseBoolean(reader.getAttributeValue(null, "mutuallyExclusive"));

        Set<QName> excludes;
        try {
            excludes = helper.parseListOfQNames(reader, "excludes");
        } catch (InvalidPrefixException e) {
            String prefix = e.getPrefix();
            URI uri = context.getContributionUri();
            InvalidQNamePrefix failure =
                    new InvalidQNamePrefix("The excludes prefix " + prefix + " specified in the definitions.xml file in contribution " + uri
                                                   + " is invalid", startLocation);
            context.addError(failure);
            return null;
        }

        Set<Qualifier> qualifiers = new HashSet<Qualifier>();
        // create the intent before the qualifiers are populated so attributes can be validated
        Intent intent= new Intent(qName, constrains, requires, qualifiers, mutuallyExclusive, excludes, intentType, false);

        validateAttributes(reader, context, intent);

        // populates the qualifiers
        boolean defaultSet = false;
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                if (QUALIFIER.equals(reader.getName())) {
                    Location location = reader.getLocation();

                    String nameAttr = reader.getAttributeValue(null, "name");
                    if (nameAttr == null) {
                        context.addError(new MissingAttribute("Qualifier name not specified", location));
                        return null;
                    }
                    String defaultStr = reader.getAttributeValue(null, "default");
                    Boolean isDefault = Boolean.valueOf(defaultStr);
                    if (isDefault) {
                        if (defaultSet) {
                            DuplicateDefaultIntent error =
                                    new DuplicateDefaultIntent("More than one qualified intent is specified as the default for: " + qName,
                                                               location);
                            context.addError(error);
                        } else {
                            defaultSet = true;
                        }
                    }
                    Qualifier qualifier = new Qualifier(nameAttr, isDefault);
                    if (qualifiers.contains(qualifier)) {
                        DuplicateQualifiedName error = new DuplicateQualifiedName("Duplicate qualified intent specified for:" + qName, location);
                        context.addError(error);
                    } else {
                        qualifiers.add(qualifier);
                    }
                }
                break;
            case END_ELEMENT:
                if (DefinitionsLoader.INTENT.equals(reader.getName())) {
                    return new Intent(qName, constrains, requires, qualifiers, mutuallyExclusive, excludes, intentType, false);
                }
            }
        }

    }

}
