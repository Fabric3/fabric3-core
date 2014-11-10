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
package org.fabric3.fabric.domain.generator.policy;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.api.model.type.definitions.AbstractPolicyDefinition;
import org.fabric3.api.model.type.definitions.BindingType;
import org.fabric3.api.model.type.definitions.ExternalAttachment;
import org.fabric3.api.model.type.definitions.ImplementationType;
import org.fabric3.api.model.type.definitions.Intent;
import org.fabric3.api.model.type.definitions.IntentMap;
import org.fabric3.api.model.type.definitions.IntentQualifier;
import org.fabric3.api.model.type.definitions.PolicySet;
import org.fabric3.api.model.type.definitions.Qualifier;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.domain.generator.policy.PolicyActivationException;
import org.fabric3.spi.domain.generator.policy.PolicyRegistry;
import org.oasisopen.sca.annotation.Reference;

/**
 * Default implementation of the policy registry.
 */
public class DefaultPolicyRegistry implements PolicyRegistry {

    private MetaDataStore metaDataStore;
    private Map<Class<? extends AbstractPolicyDefinition>, Map<QName, ? extends AbstractPolicyDefinition>> cache
            = new ConcurrentHashMap<>();

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
        Set<PolicySet> policySets = new HashSet<>();
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
        Set<D> set = new HashSet<>();
        for (D value : subCache.values()) {
            if (names.contains(value.getName())) {
                set.add(value);
            }
        }
        return set;
    }

    public Set<PolicySet> activateDefinitions(URI uri) throws PolicyActivationException {
        Contribution contribution = metaDataStore.find(uri);
        Set<Intent> intents = new HashSet<>();
        Set<PolicySet> policySets = new HashSet<>();
        Set<PolicySet> attachedPolicySets = new HashSet<>();
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
                } else if (value instanceof ExternalAttachment) {
                    ExternalAttachment externalAttachment = (ExternalAttachment) value;
                    activate(externalAttachment);
                }
            }
        }
        validateIntents(intents);
        validatePolicySets(policySets);
        return attachedPolicySets;
    }

    public Set<PolicySet> deactivateDefinitions(URI uri) throws PolicyActivationException {
        Set<PolicySet> policySets = new HashSet<>();
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
        } else if (definition instanceof ExternalAttachment) {
            getSubCache(ExternalAttachment.class).remove(definition.getName());
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

    private void activate(ExternalAttachment externalAttachment) {
        Map<QName, ExternalAttachment> subCache = getSubCache(ExternalAttachment.class);
        QName name = externalAttachment.getName();
        subCache.put(name, externalAttachment);
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
                        throw new PolicyActivationException(
                                "Intent map that provides " + intentMap.getProvides() + " in policy set " + name + " does not specify the qualifier "
                                + qualifierName);
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
                            throw new PolicyActivationException("Referenced policy set " + referenceName + " from " + name + " provides an intent " + provided
                                                                + " that is not provided by the parent policy set");
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <D extends AbstractPolicyDefinition> Map<QName, D> getSubCache(Class<D> definitionClass) {
        Map<QName, D> map = (Map<QName, D>) cache.get(definitionClass);
        if (map == null) {
            map = new HashMap<>();
            cache.put(definitionClass, map);
        }
        return map;
    }

}
