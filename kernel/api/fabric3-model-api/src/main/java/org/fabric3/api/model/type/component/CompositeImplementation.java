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
package org.fabric3.api.model.type.component;

import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;

/**
 * A composite component implementation.
 */
public class CompositeImplementation extends Implementation<Composite> {
    private static final long serialVersionUID = 2140686609936627287L;
    private static final QName IMPLEMENTATION_COMPOSITE = new QName(Constants.SCA_NS, "implementation.composite");
    private QName name;

    public QName getType() {
        return IMPLEMENTATION_COMPOSITE;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public String getArtifactName() {
        return name == null ? null : name.toString();
    }


}
