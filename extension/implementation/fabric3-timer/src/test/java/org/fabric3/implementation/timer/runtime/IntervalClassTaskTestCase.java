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
package org.fabric3.implementation.timer.runtime;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 *
 */
public class IntervalClassTaskTestCase extends TestCase {

    public void testNextInterval() throws Exception {
        Interval interval = EasyMock.createMock(Interval.class);
        EasyMock.expect(interval.nextInterval()).andReturn((long) 1000);
        Runnable delegate = EasyMock.createMock(Runnable.class);

        EasyMock.replay(interval, delegate);

        IntervalClassTask task = new IntervalClassTask(interval, delegate);
        assertEquals(1000, task.nextInterval());

        EasyMock.verify(interval, delegate);
    }

    public void testRun() throws Exception {
        Interval interval = EasyMock.createMock(Interval.class);
        Runnable delegate = EasyMock.createMock(Runnable.class);
        delegate.run();

        EasyMock.replay(interval, delegate);

        IntervalClassTask task = new IntervalClassTask(interval, delegate);
        task.run();

        EasyMock.verify(interval, delegate);
    }

    private interface Interval {
        long nextInterval();
    }
}

