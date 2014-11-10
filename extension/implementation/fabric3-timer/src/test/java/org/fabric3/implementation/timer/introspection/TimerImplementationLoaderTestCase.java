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
package org.fabric3.implementation.timer.introspection;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.implementation.java.introspection.JavaImplementationIntrospector;
import org.fabric3.api.implementation.timer.model.TimerImplementation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 *
 */
public class TimerImplementationLoaderTestCase extends TestCase {
    TimerImplementationLoader loader;
    private static final String RECURRING = "<implementation.timer intervalClass='" + TestInterval.class.getName() + "' class='" + TestTimer.class.getName()
                                            + "' />";

    private static final String FIXED = "<implementation.timer fixedRate='1000' unit='seconds' initialDelay='2000' class='" + TestTimer.class.getName()
                                        + "' />";

    private static final String INTERVAL = "<implementation.timer repeatInterval='1000' unit='seconds' initialDelay='2000' class='" + TestTimer.class.getName()
                                           + "' />";

    private static final String FIRE_ONCE = "<implementation.timer fireOnce='1000' unit='seconds' class='" + TestTimer.class.getName() + "' />";

    private static final String ILLEGAL_ATTRIBUTE = "<implementation.timer fireOnce='1000' foo='seconds' class='" + TestTimer.class.getName() + "' />";

    private static final String ILLEGAL_FIXED_AND_REPEAT = "<implementation.timer fixedRate='1000' repeatInterval='2000' class='" + TestTimer.class.getName()
                                                           + "' />";

    private static final String INVALID_TIMER_IMPLEMENTATION = "<implementation.timer intervalClass='" + TestInterval.class.getName() + "' class='"
                                                               + TestCase.class.getName() + "' />";

    private static final String INVALID_INTERVAL_CLASS = "<implementation.timer intervalClass='" + TestCase.class.getName() + "' class='"
                                                         + TestTimer.class.getName() + "' />";

    private static final String INTERVAL_METHOD = "<implementation.timer class='" + TestIntervalTimer.class.getName() + "' />";

    private static final String INVALID_INTERVAL_METHOD = "<implementation.timer class='" + TestInvalidIntervalTimer.class.getName() + "' />";

    private XMLInputFactory xmlFactory;
    private DefaultIntrospectionContext context;

    public void testRecurringScheduleLoad() throws Exception {
        TimerImplementation implementation = loader.load(createReader(RECURRING), context);
        assertEquals(TestTimer.class.getName(), implementation.getImplementationClass());
        assertEquals(TestInterval.class.getName(), implementation.getTimerData().getIntervalClass());
        assertFalse(context.hasErrors());
    }

    public void testFixedLoad() throws Exception {
        TimerImplementation implementation = loader.load(createReader(FIXED), context);
        assertEquals(TestTimer.class.getName(), implementation.getImplementationClass());
        assertEquals(1000, implementation.getTimerData().getFixedRate());
        assertEquals(2000, implementation.getTimerData().getInitialDelay());
        assertEquals(TimeUnit.SECONDS, implementation.getTimerData().getTimeUnit());
        assertFalse(context.hasErrors());
    }

    public void testRepeatIntervalLoad() throws Exception {
        TimerImplementation implementation = loader.load(createReader(INTERVAL), context);
        assertEquals(TestTimer.class.getName(), implementation.getImplementationClass());
        assertEquals(1000, implementation.getTimerData().getRepeatInterval());
        assertEquals(2000, implementation.getTimerData().getInitialDelay());
        assertEquals(TimeUnit.SECONDS, implementation.getTimerData().getTimeUnit());
        assertFalse(context.hasErrors());
    }

    public void testRepeatFireOnceLoad() throws Exception {
        TimerImplementation implementation = loader.load(createReader(FIRE_ONCE), context);
        assertEquals(TestTimer.class.getName(), implementation.getImplementationClass());
        assertEquals(1000, implementation.getTimerData().getFireOnce());
        assertEquals(TimeUnit.SECONDS, implementation.getTimerData().getTimeUnit());
        assertFalse(context.hasErrors());
    }

    public void testIllegalAttribute() throws Exception {
        loader.load(createReader(ILLEGAL_ATTRIBUTE), context);
        assertTrue(context.hasErrors());
        assertEquals(UnrecognizedAttribute.class, context.getErrors().get(0).getClass());
    }

    public void testIllegalFixedAndRepeat() throws Exception {
        loader.load(createReader(ILLEGAL_FIXED_AND_REPEAT), context);
        assertTrue(context.hasErrors());
        assertEquals(InvalidTimerExpression.class, context.getErrors().get(0).getClass());
    }

    public void testInvalidTimerImplementation() throws Exception {
        loader.load(createReader(INVALID_TIMER_IMPLEMENTATION), context);
        assertTrue(context.hasErrors());
        assertEquals(InvalidTimerInterface.class, context.getErrors().get(0).getClass());
    }

    public void testInvalidIntervalClass() throws Exception {
        loader.load(createReader(INVALID_INTERVAL_CLASS), context);
        assertTrue(context.hasErrors());
        assertEquals(InvalidIntervalClass.class, context.getErrors().get(0).getClass());
    }

    public void testIntervalMethod() throws Exception {
        TimerImplementation implementation = loader.load(createReader(INTERVAL_METHOD), context);
        assertFalse(context.hasErrors());
        assertTrue(implementation.getTimerData().isIntervalMethod());
    }

    public void testInvalidIntervalMethod() throws Exception {
        loader.load(createReader(INVALID_INTERVAL_METHOD), context);
        assertTrue(context.hasErrors());
        assertEquals(InvalidIntervalMethod.class, context.getErrors().get(0).getClass());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        xmlFactory = XMLInputFactory.newInstance();
        context = new DefaultIntrospectionContext(URI.create("test"), getClass().getClassLoader());
        JavaImplementationIntrospector processor = EasyMock.createMock(JavaImplementationIntrospector.class);
        processor.introspect(EasyMock.isA(InjectingComponentType.class), EasyMock.eq(context));
        EasyMock.replay(processor);
        LoaderHelper helper = EasyMock.createNiceMock(LoaderHelper.class);
        loader = new TimerImplementationLoader(processor, helper);
    }

    private XMLStreamReader createReader(String xml) throws XMLStreamException {
        InputStream in = new ByteArrayInputStream(xml.getBytes());
        XMLStreamReader reader = xmlFactory.createXMLStreamReader(in);
        reader.nextTag();
        return reader;
    }

}