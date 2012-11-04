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
package org.fabric3.fabric.generator.policy;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.model.type.definitions.BindingType;
import org.fabric3.model.type.definitions.ImplementationType;
import org.fabric3.model.type.definitions.Intent;
import org.fabric3.model.type.definitions.IntentMap;
import org.fabric3.model.type.definitions.IntentQualifier;
import org.fabric3.model.type.definitions.PolicySet;
import org.fabric3.model.type.definitions.Qualifier;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.generator.policy.PolicyActivationException;
import org.fabric3.spi.generator.policy.PolicyRegistry;

/**
 * Default implementation of the policy registry.
 */
public class DefaultPolicyRegistry implements PolicyRegistry {

    private MetaDataStore metaDataStore;
    private Map<Class<? extends AbstractPolicyDefinition>, Map<QName, ? extends AbstractPolicyDefinition>> cache =
            new ConcurrentHashMap<Class<? extends AbstractPolicyDefinition>, Map<QName, ? extends AbstractPolicyDefinition>>();

    /**
     * Initializes the cache.
     *
     * @param metaDataStore the metadata store
     */
    public DefaultPolicyRegistry(@Reference MetaDataStore metaDataStore) {
        this.metaDataStore = metaDataStore;
        cache.put(Intent.class, new ConcurrentHashMap<QName, Intent>());
        cache.put(PolicySet.class, new ConcurrentHashMap<QName, PolicySet>());
        cache.put(BindingType.class, new ConcurrentHashMap<QName, BindingType>());
        cache.put(ImplementationType.class, new ConcurrentHashMap<QName, ImplementationType>());
    }

    public <D extends AbstractPolicyDefinition> Collection<D> getAllDefinitions(Class<D> definitionClass) {
        return getSubCache(definitionClass).values();
    }

    public Set<PolicySet> getExternalAttachmentPolicies() {
        Map<QName, PolicySet> subCache = getSubCache(PolicySet.class);
        Set<PolicySet> policySets = new HashSet<PolicySet>();
        for (PolicySet policySet : subCache.values()) {
            if (policySet.getAttachTo() != null) {
                policySets.add(policySet);
            }
        }
        return policySets;
    }

    public <D extends AbstractPolicyDefinition> D getDefinition(QName name, Class<D> definitionClass) {
        return getSubCache(definitionClass).get(name);
    }

    public <D extends AbstractPolicyDefinition> Set<D> getDefinitions(Set<QName> names, Class<D> definitionClass) {
        Map<QName, D> subCache = getSubCache(definitionClass);
        if (subCache == null) {
            return Collections.emptySet();
        }
        return new HashSet<D>(subCache.values());
    }

    public Set<PolicySet> activateDefinitions(URI uri) throws PolicyActivationException {
        Contribution contribution = metaDataStore.find(uri);
        Set<Intent> intents = new HashSet<Intent>();
        Set<PolicySet> policySets = new HashSet<PolicySet>();
        Set<PolicySet> attachedPolicySets = new HashSet<PolicySet>();
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> resourceElement : resource.getResourceElements()) {
                Object value = resourceElement.getValue();
                if (value instanceof Intent) {
                    Intent intent = (Intent) value;
                    activate(intent);
                    intents.add(intent);
                } else if (value instanceof PolicySet) {
                    PolicySet policySet = (PolicySet) value;
                    activate(policySet);
                    if (policySet.getAttachTo() != null) {
                        attachedPolicySets.add(policySet);
                    }
                    policySets.add(policySet);
                } else if (value instanceof BindingType) {
                    BindingType bindingType = (BindingType) value;
                    activate(bindingType);
                } else if (value instanceof ImplementationType) {
                    ImplementationType implementationType = (ImplementationType) value;
                    activate(implementationType);
                }
            }
        }
        validateIntents(intents);
        validatePolicySets(policySets);
        return attachedPolicySets;
    }

    public Set<PolicySet> deactivateDefinitions(URI uri) throws PolicyActivationException {
        Set<PolicySet> policySets = new HashSet<PolicySet>();
        Contribution contribution = metaDataStore.find(uri);
        for (Resource resource : contribution.getResources()) {
            for (ResourceElement<?, ?> resourceElement : resource.getResourceElements()) {
                Object value = resourceElement.getValue();
                if (value instanceof AbstractPolicyDefinition) {
                    AbstractPolicyDefinition definition = (AbstractPolicyDefinition) value;
                    deactivate(definition);
                    if (definition instanceof PolicySet) {
                        PolicySet policySet = (PolicySet) definition;
                        if (policySet.getAttachTo() != null) {
                            policySets.add(policySet);
                        }
                    }
                }
            }
        }
        return policySets;
    }

    /**
     * Deactivates the policy definition.
     *
     * @param definition the definition
     */
    private void deactivate(AbstractPolicyDefinition definition) {
        if (definition instanceof Intent) {
            getSubCache(Intent.class).remove(definition.getName());
        } else if (definition instanceof PolicySet) {
            getSubCache(PolicySet.class).remove(definition.getName());
        } else if (definition instanceof BindingType) {
            getSubCache(BindingType.class).remove(definition.getName());
        } else if (definition instanceof ImplementationType) {
            getSubCache(ImplementationType.class).remove(definition.getName());
        }
    }

    private void activate(Intent intent) throws PolicyActivationException {
        Map<QName, Intent> subCache = getSubCache(Intent.class);
        QName name = intent.getName();
        if (subCache.containsKey(name)) {
            throw new PolicyActivationException("Duplicate intent found:" + name);
        }
        subCache.put(name, intent);
    }

    private void activate(PolicySet policySet) throws PolicyActivationException {
        Map<QName, PolicySet> subCache = getSubCache(PolicySet.class);
        QName name = policySet.getName();
        if (subCache.containsKey(name)) {
            throw new PolicyActivationException("Duplicate policy set found:" + name);
        }
        subCache.put(name, policySet);
    }

    private void activate(BindingType bindingType) throws PolicyActivationException {
        Map<QName, BindingType> subCache = getSubCache(BindingType.class);
        QName name = bindingType.getName();
        if (subCache.containsKey(name)) {
            throw new PolicyActivationException("Duplicate binding type found:" + name);
        }
        subCache.put(name, bindingType);
    }

    private void activate(ImplementationType implementationType) throws PolicyActivationException {
        Map<QName, ImplementationType> subCache = getSubCache(ImplementationType.class);
        QName name = implementationType.getName();
        if (subCache.containsKey(name)) {
            throw new PolicyActivationException("Duplicate implementation type found:" + name);
        }
        subCache.put(name, implementationType);
    }

    private void validateIntents(Set<Intent> intents) throws PolicyActivationException {
        Map<QName, Intent> subCache = getSubCache(Intent.class);
        for (Intent intent : intents) {
            // verify required intents exist
            for (QName required : intent.getRequires()) {
                if (!subCache.containsKey(required)) {
                    throw new PolicyActivationException("Required intent specified in " + intent.getName() + " not found: " + required);
                }
            }
            // verify excluded intents exist
            for (QName excluded : intent.getExcludes()) {
                if (!subCache.containsKey(excluded)) {
                    throw new PolicyActivationException("Excluded intent specified in " + intent.getName() + " not found: " + excluded);
                }
            }
        }
    }

    private void validatePolicySets(Set<PolicySet> policySets) throws PolicyActivationException {
        Map<QName, Intent> intentCache = getSubCache(Intent.class);
        for (PolicySet policySet : policySets) {
            for (IntentMap intentMap : policySet.getIntentMaps()) {
                QName provides = intentMap.getProvides();
                Intent intent = intentCache.get(provides);
                if (intent == null) {
                    QName name = policySet.getName();
                    throw new PolicyActivationException("Intent " + provides + " specified as the provided intent for " + name + " was not found");
                }
                for (Qualifier qualifier : intent.getQualifiers()) {
                    String qualifierName = qualifier.getName();
                    boolean found = false;
                    for (IntentQualifier intentQualifier : intentMap.getQualifiers()) {
                        if (intentQualifier.getName().equals(qualifierName)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        QName name = policySet.getName();
                        throw new PolicyActivationException("Intent map that provides " + intentMap.getProvides() + " in policy set " + name
                                + " does not specify the qualifier " + qualifierName);
                    }
                }
            }
            if (!policySet.getPolicySetReferences().isEmpty()) {
                Map<QName, PolicySet> policySetCache = getSubCache(PolicySet.class);
                for (QName referenceName : policySet.getPolicySetReferences()) {
                    PolicySet referencedPolicySet = policySetCache.get(referenceName);
                    if (referencedPolicySet == null) {
                        QName name = policySet.getName();
                        throw new PolicyActivationException("Referenced policy set " + referenceName + " from " + name + " was not found");
                    }
                    for (QName provided : referencedPolicySet.getProvidedIntents()) {
                        if (!policySet.doesProvide(provided)) {
                            QName name = policySet.getName();
                            throw new PolicyActivationException("Referenced policy set " + referenceName + " from " + name + " provides an intent "
                                    + provided + " that is not provided by the parent policy set");
                        }
                    }
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    private <D extends AbstractPolicyDefinition> Map<QName, D> getSubCache(Class<D> definitionClass) {
        return (Map<QName, D>) cache.get(definitionClass);
    }

}
