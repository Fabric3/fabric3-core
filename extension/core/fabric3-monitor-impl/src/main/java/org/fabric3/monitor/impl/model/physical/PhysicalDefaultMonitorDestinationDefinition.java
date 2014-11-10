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
package org.fabric3.monitor.impl.model.physical;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.monitor.spi.model.physical.PhysicalAppenderDefinition;
import org.fabric3.monitor.spi.model.physical.PhysicalMonitorDestinationDefinition;

/**
 * Default monitor destination configuration.
 */
public class PhysicalDefaultMonitorDestinationDefinition extends PhysicalMonitorDestinationDefinition {
    private static final long serialVersionUID = -7786050402412624284L;

    private List<PhysicalAppenderDefinition> definitions = new ArrayList<>();

    public PhysicalDefaultMonitorDestinationDefinition(String name) {
        super(name);
    }

    public List<PhysicalAppenderDefinition> getDefinitions() {
        return definitions;
    }

    public void add(PhysicalAppenderDefinition definition) {
        definitions.add(definition);
    }
}
