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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.model.type.definitions;

import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

/**
 * An intent.
 *
 * @version $Rev$ $Date$
 */
public final class Intent extends AbstractPolicyDefinition {
    private static final long serialVersionUID = -3075153359030949561L;

    public static final QName BINDING = new QName(Constants.SCA_NS, "binding");
    public static final QName IMPLEMENTATION = new QName(Constants.SCA_NS, "implementation");

    private IntentType intentType;
    private QName qualifiable;
    private Set<QName> requires;
    private QName constrains;
    private Set<Qualifier> qualifiers;
    private boolean defaultIntent;
    private boolean mutuallyExclusive;
    private Set<QName> excludes;

    /**
     * Initializes the name, description and the constrained artifacts.
     *
     * @param name              the name of the intent
     * @param constrains        the SCA artifact constrained by this intent
     * @param requires          the intents this intent requires if this is a profile intent
     * @param qualifiers        any qualifiers defined inline using the <qualifier> element
     * @param mutuallyExclusive true if the qualified intents are mutually exclusive
     * @param excludes          the list of intents excluded by this one
     * @param intentType        the intent type (interaction or implementation)
     * @param defaultIntent     true if this is a default qualified intent
     */
    public Intent(QName name,
                  QName constrains,
                  Set<QName> requires,
                  Set<Qualifier> qualifiers,
                  boolean mutuallyExclusive,
                  Set<QName> excludes,
                  IntentType intentType,
                  boolean defaultIntent) {
        super(name);
        this.qualifiers = qualifiers;
        this.mutuallyExclusive = mutuallyExclusive;
        this.excludes = excludes;
        this.defaultIntent = defaultIntent;
        if (constrains != null) {
            if (!BINDING.equals(constrains) && !IMPLEMENTATION.equals(constrains)) {
                throw new IllegalArgumentException("Intents can constrain only bindings or implementations");
            }
            this.constrains = constrains;
        }
        this.intentType = intentType;
        String localPart = name.getLocalPart();
        if (localPart.indexOf('.') > 0) {
            String qualifiableName = localPart.substring(0, localPart.indexOf('.') + 1);
            qualifiable = new QName(name.getNamespaceURI(), qualifiableName);
        }
        this.requires = requires;
    }

    /**
     * Checks whether this is a profile intent.
     *
     * @return True if this is a profile intent.
     */
    public boolean isProfile() {
        return requires != null && requires.size() > 0;
    }

    /**
     * The intents this intent requires if this is a profile intent.
     *
     * @return Required intents for a profile intent.
     */
    public Set<QName> getRequires() {
        return requires;
    }

    /**
     * Checks whether this is a qualified intent.
     *
     * @return True if this is a qualified intent.
     */
    public boolean isQualified() {
        return qualifiable != null;
    }

    /**
     * Returns the qualifiable intent if this is qualified.
     *
     * @return Name of the qualifiable intent.
     */
    public QName getQualifiable() {
        return qualifiable;
    }

    /**
     * Returns the type of this intent.
     *
     * @return Type of the intent.
     */
    public IntentType getIntentType() {
        return intentType;
    }

    /**
     * Returns a set of qualifiers defined inline using the <qualifier> element. Note that this method does not return qualifiers defined using a
     * distinct <intent> element.
     *
     * @return the qualified intents
     */
    public Set<Qualifier> getQualifiers() {
        return qualifiers;
    }

    /**
     * True if the qualified intents are mutually exclusive
     *
     * @return true if the qualified intents are mutually exclusive
     */
    public boolean isMutuallyExclusive() {
        return mutuallyExclusive;
    }

    /**
     * Returns the set of intents excluded by this intent.
     *
     * @return the set of intents excluded by this intent.
     */
    public Set<QName> getExcludes() {
        return excludes;
    }

    /**
     * Whether this intent constrains the specified type.
     *
     * @param type the type of the SCA artifact.
     * @return True if this intent constrains the specified type.
     */
    public boolean doesConstrain(QName type) {
        return type.equals(constrains);
    }

    /**
     * Returns true if this is a default qualified intent.
     *
     * @return true if this is a default qualified intent
     */
    public boolean isDefault() {
        return defaultIntent;
    }

    /**
     * Returns what this intent constrains.
     *
     * @return what this intent constrains
     */
    public QName getConstrains() {
        return constrains;
    }
}
