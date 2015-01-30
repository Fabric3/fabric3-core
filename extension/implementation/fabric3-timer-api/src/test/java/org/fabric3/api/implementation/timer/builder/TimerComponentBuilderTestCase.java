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
package org.fabric3.api.implementation.timer.builder;

import junit.framework.TestCase;
import org.fabric3.api.implementation.timer.model.TimerType;

/**
 *
 */
public class TimerComponentBuilderTestCase extends TestCase {

    public void testTypes() throws Exception {
        TimerComponentBuilder.newBuilder(Foo.class, TimerType.ONCE).fireOnce(100);
        try {
            TimerComponentBuilder.newBuilder(Foo.class, TimerType.INTERVAL).fireOnce(100);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            TimerComponentBuilder.newBuilder(Foo.class, TimerType.FIXED_RATE).fireOnce(100);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            TimerComponentBuilder.newBuilder(Foo.class, TimerType.RECURRING).fireOnce(100);
            fail();
        } catch (IllegalArgumentException e) {
            // expected
        }

    }

    private class Foo implements Runnable {

        public void run() {
        }
    }
}
