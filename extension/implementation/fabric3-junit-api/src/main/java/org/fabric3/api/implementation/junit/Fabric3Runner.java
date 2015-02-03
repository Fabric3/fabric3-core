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
package org.fabric3.api.implementation.junit;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

/**
 * Runner used to indicate Fabric3 integration test components.
 *
 * The runner ignores tests when executed during the unit test phase of a build (or from an IDE test runner) as the tests will be run during the integration
 * test phase of a build via a plugin.
 */
public class Fabric3Runner extends Runner {
    private Class<?> testClass;

    public Fabric3Runner(Class<?> testClass) {
        this.testClass = testClass;
    }

    public Description getDescription() {
        return Description.createSuiteDescription("F3 integration tests");
    }

    public void run(RunNotifier notifier) {
        notifier.fireTestIgnored(Description.createSuiteDescription(testClass.getName() + " [Fabric3 integration tests]"));
    }
}
