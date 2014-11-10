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
package org.fabric3.test.spi;

import java.net.URI;
import java.util.Map;

import org.fabric3.api.host.Names;
import org.fabric3.spi.container.wire.Wire;

/**
 * Provides wires to test components used by a integration test runtime for test dispatching.
 */
public interface TestWireHolder {

    URI COMPONENT_URI = URI.create(Names.RUNTIME_NAME + "/TestWireHolder");

    /**
     * Adds a wire to a test component keyed by test name.
     *
     * @param testName the test name
     * @param wire     the wire
     */
    void add(String testName, Wire wire);


    /**
     * The wires to test components keyed by test name.
     *
     * @return wires to test components keyed by test name
     */
    Map<String, Wire> getWires();

}
