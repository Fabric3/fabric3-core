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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.model.type.definitions;

import java.util.Set;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

/**
 * An intent.
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
