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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.api.model.type.AbstractPolicyAware;
import org.fabric3.api.model.type.CapabilityAware;

/**
 * Base binding representation.
 */
public abstract class BindingDefinition extends AbstractPolicyAware<BindableDefinition> implements CapabilityAware {
    private static final long serialVersionUID = 8780407747984243865L;

    protected URI targetUri;
    protected QName type;
    protected String name;

    private Set<String> requiredCapabilities = new HashSet<>();
    private List<BindingHandlerDefinition> handlers = new ArrayList<>();

    /**
     * Constructor for a binding using the default binding name.
     *
     * @param targetUri the target URI which may be null if not specified
     * @param type      the binding type
     */
    public BindingDefinition(URI targetUri, QName type) {
        this.targetUri = targetUri;
        this.type = type;
    }

    /**
     * Constructor for a binding using a configured binding name.
     *
     * @param name      the binding name
     * @param targetUri the target URI which may be null if not specified
     * @param type      the binding type
     */
    public BindingDefinition(String name, URI targetUri, QName type) {
        this.name = name;
        this.targetUri = targetUri;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public QName getType() {
        return type;
    }

    public Set<String> getRequiredCapabilities() {
        return requiredCapabilities;
    }

    public void addRequiredCapability(String capability) {
        requiredCapabilities.add(capability);
    }

    /**
     * Returns the applicable binding handlers for this definition. Note that order is significant: at runtime, the handlers should be engaged in the order they
     * appear in this list.
     *
     * @return the applicable binding handlers for this definition.
     */
    public List<BindingHandlerDefinition> getHandlers() {
        return handlers;
    }

    /**
     * Adds a binding handler definition.
     *
     * @param handler the binding handler definition
     */
    public void addHandler(BindingHandlerDefinition handler) {
        handlers.add(handler);
    }

}
