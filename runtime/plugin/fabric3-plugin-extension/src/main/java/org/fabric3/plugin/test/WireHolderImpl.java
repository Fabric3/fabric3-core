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
package org.fabric3.plugin.test;

import java.util.LinkedHashMap;
import java.util.Map;

import org.fabric3.spi.container.wire.Wire;
import org.fabric3.test.spi.TestWireHolder;

/**
 *
 */
public class WireHolderImpl implements TestWireHolder {
    private Map<String, Wire> wires = new LinkedHashMap<>();

    public Map<String, Wire> getWires() {
        return wires;
    }

    public void add(String testName, Wire wire) {
        wires.put(testName, wire);
    }
}
