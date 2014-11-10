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

/**
 * A policy type.
 */
public class AbstractPolicyType extends AbstractPolicyDefinition {
    private static final long serialVersionUID = -2910491671004468756L;

    private final Set<QName> alwaysProvide;
    private final Set<QName> mayProvide;

    /**
     * @param name          Name of the binding type.
     * @param alwaysProvide Intents this binding always provide.
     * @param mayProvide    Intents this binding may provide.
     */
    public AbstractPolicyType(QName name, Set<QName> alwaysProvide, Set<QName> mayProvide) {
        super(name);
        this.alwaysProvide = alwaysProvide;
        this.mayProvide = mayProvide;
    }

    /**
     * @return Intents this binding always provide.
     */
    public Set<QName> getAlwaysProvide() {
        return alwaysProvide;
    }

    /**
     * @return Intents this binding may provide.
     */
    public Set<QName> getMayProvide() {
        return mayProvide;
    }


}
