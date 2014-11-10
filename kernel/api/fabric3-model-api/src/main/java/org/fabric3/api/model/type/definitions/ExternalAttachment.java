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
package org.fabric3.api.model.type.definitions;

import javax.xml.namespace.QName;
import java.util.Set;
import java.util.UUID;

import org.oasisopen.sca.Constants;

/**
 * An external attachment specified in a policy definitions file.
 */
public class ExternalAttachment extends AbstractPolicyDefinition {
    private static final long serialVersionUID = -6816479833424275158L;
    private String attachTo;
    private Set<QName> policySets;
    private Set<QName> intents;

    public ExternalAttachment(String attachTo, Set<QName> policySets, Set<QName> intents) {
        setName(new QName(Constants.SCA_NS, UUID.randomUUID().toString()));
        this.attachTo = attachTo;
        this.policySets = policySets;
        this.intents = intents;
    }

    public String getAttachTo() {
        return attachTo;
    }

    public Set<QName> getPolicySets() {
        return policySets;
    }

    public Set<QName> getIntents() {
        return intents;
    }
}
