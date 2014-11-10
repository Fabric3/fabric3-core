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
 */
package org.fabric3.api.model.type.component;

import java.io.Serializable;

/**
 * Encapsulates information needed to identify a component/service, component/service/binding, component/reference or component/reference/binding.
 * Targets are relative to a composite and not absolute.
 */
public class Target implements Serializable {
    private static final long serialVersionUID = 8616545726099554138L;

    private String component;
    private String bindable;
    private String binding;

    public Target(String component) {
        this.component = component;
    }

    public Target(String component, String bindable) {
        this.component = component;
        this.bindable = bindable;
    }

    public Target(String component, String bindable, String binding) {
        this.component = component;
        this.bindable = bindable;
        this.binding = binding;
    }

    /**
     * Returns the component name.
     *
     * @return the component name
     */
    public String getComponent() {
        return component;
    }

    /**
     * Returns the reference or service name.
     *
     * @return the reference or service name. May be null.
     */
    public String getBindable() {
        return bindable;
    }

    /**
     * Returns the binding name.
     *
     * @return the binding name. May be null.
     */
    public String getBinding() {
        return binding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        return !(bindable != null ? !bindable.equals(target.bindable) : target.bindable != null)
                && !(binding != null ? !binding.equals(target.binding) : target.binding != null)
                && !(component != null ? !component.equals(target.component) : target.component != null);

    }

    @Override
    public int hashCode() {
        int result = component != null ? component.hashCode() : 0;
        result = 31 * result + (bindable != null ? bindable.hashCode() : 0);
        result = 31 * result + (binding != null ? binding.hashCode() : 0);
        return result;
    }
}
