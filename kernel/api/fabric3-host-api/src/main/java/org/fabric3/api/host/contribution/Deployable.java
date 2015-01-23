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
package org.fabric3.api.host.contribution;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.api.model.type.RuntimeMode;

/**
 * Represents a deployable artifact in a contribution
 */
@SuppressWarnings({"SerializableHasSerializationMethods"})
public class Deployable implements Serializable {
    private static final long serialVersionUID = -710863113841788110L;
    public static final List<RuntimeMode> DEFAULT_MODES;

    static {
        DEFAULT_MODES = new ArrayList<>();
        DEFAULT_MODES.add(RuntimeMode.VM);
        DEFAULT_MODES.add(RuntimeMode.NODE);
    }

    private QName name;
    private List<RuntimeMode> runtimeModes;
    private List<String> environments;


    public Deployable(QName name) {
        this(name, DEFAULT_MODES, Collections.<String>emptyList());
    }

    public Deployable(QName name, List<RuntimeMode> runtimeModes, List<String> environments) {
        this.name = name;
        this.runtimeModes = runtimeModes;
        this.environments = environments;
    }

    /**
     * The QName of the deployable component.
     *
     * @return QName of the deployable component
     */
    public QName getName() {
        return name;
    }

    /**
     * Returns the runtime modes the deployable should be activated in.
     *
     * @return the runtime modes the deployable should be activated in.
     */
    public List<RuntimeMode> getRuntimeModes() {
        return runtimeModes;
    }

    /**
     * Returns the runtime environments this deployable should be activated in. An empty list indicates all environments.
     *
     * @return the runtime environments this deployable should be activated in
     */
    public List<String> getEnvironments() {
        return environments;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Deployable that = (Deployable) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
