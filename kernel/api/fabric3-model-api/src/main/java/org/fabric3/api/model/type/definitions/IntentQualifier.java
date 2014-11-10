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

import java.io.Serializable;

import org.w3c.dom.Element;

/**
 * A qualifier in an IntentMap.
 */
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public final class IntentQualifier implements Serializable {
    private static final long serialVersionUID = -2487001541970876844L;

    private String name;
    private Element content;

    public IntentQualifier(String name, Element content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public Element getContent() {
        return content;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntentQualifier that = (IntentQualifier) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
