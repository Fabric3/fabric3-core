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
package org.fabric3.implementation.junit.model;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.implementation.junit.common.ContextConfiguration;

/**
 *
 */
public class JUnitBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -1306543849900003084L;
    private ContextConfiguration configuration;

    /**
     * Constructor.
     *
     * @param configuration the context configuration or null if not set
     */
    public JUnitBindingDefinition(ContextConfiguration configuration) {
        super(null, "junit");
        this.configuration = configuration;
    }

    /**
     * Returns the context configuration that must be established prior to an invocation or null if a context is not configured.
     *
     * @return the context configuration or null
     */
    public ContextConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Sets the context configuration that must be established prior to an invocation or null if a context is not configured.
     *
     * @param configuration the configuration
     */
    public void setConfiguration(ContextConfiguration configuration) {
        this.configuration = configuration;
    }

}
