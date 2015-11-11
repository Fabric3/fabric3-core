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
package org.fabric3.implementation.junit.provision;

import org.fabric3.implementation.junit.common.ContextConfiguration;
import org.fabric3.spi.model.physical.PhysicalWireSource;

/**
 * A wire source for the JUnit implementation type.
 */
public class JUnitWireSource extends PhysicalWireSource {
    private String testName;
    private boolean transactional;
    private ContextConfiguration configuration;

    /**
     * Constructor.
     *
     * @param testName      the test name to execute
     * @param transactional true if the test should be invoked withing a transaction
     * @param configuration the context configuration or null if a context is not configured
     */
    public JUnitWireSource(String testName, boolean transactional, ContextConfiguration configuration) {
        this.testName = testName;
        this.transactional = transactional;
        this.configuration = configuration;
    }

    /**
     * Returns the test name.
     *
     * @return the test name
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Returns true if the test should be invoked withing a transaction.
     *
     * @return true if the test should be invoked withing a transaction
     */
    public boolean isTransactional() {
        return transactional;
    }

    /**
     * Returns the context configuration that must be established prior to an invocation or null if a context is not configured.
     *
     * @return the context configuration or null
     */
    public ContextConfiguration getConfiguration() {
        return configuration;
    }

}

