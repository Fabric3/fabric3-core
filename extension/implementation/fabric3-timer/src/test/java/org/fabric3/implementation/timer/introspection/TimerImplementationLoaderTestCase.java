/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.implementation.timer.introspection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.implementation.java.introspection.JavaImplementationProcessor;
import org.fabric3.implementation.timer.model.TimerImplementation;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 *
 */
public class TimerImplementationLoaderTestCase extends TestCase {
    TimerImplementationLoader loader;
    private static final String RECURRING =
            "<implementation.timer intervalClass='" + TestInterval.class.getName() + "' class='" + TestTimer.class.getName() + "' />";

    private static final String FIXED =
            "<implementation.timer fixedRate='1000' unit='seconds' initialDelay='2000' class='" + TestTimer.class.getName() + "' />";

    private static final String INTERVAL =
            "<implementation.timer repeatInterval='1000' unit='seconds' initialDelay='2000' class='" + TestTimer.class.getName() + "' />";

    private static final String FIRE_ONCE =
            "<implementation.timer fireOnce='1000' unit='seconds' class='" + TestTimer.class.getName() + "' />";

    private static final String ILLEGAL_ATTRIBUTE =
            "<implementation.timer fireOnce='1000' foo='seconds' class='" + TestTimer.class.getName() + "' />";

    private static final String ILLEGAL_FIXED_AND_REPEAT =
            "<implementation.timer fixedRate='1000' repeatInterval='2000' class='" + TestTimer.class.getName() + "' />";

    private static final String INVALID_TIMER_IMPLEMENTATION =
            "<implementation.timer intervalClass='" + TestInterval.class.getName() + "' class='" + TestCase.class.getName() + "' />";

    private static final String INVALID_INTERVAL_CLASS =
            "<implementation.timer intervalClass='" + TestCase.class.getName() + "' class='" + TestTimer.class.getName() + "' />";

    private static final String INTERVAL_METHOD =
            "<implementation.timer class='" + TestIntervalTimer.class.getName() + "' />";

    private static final String INVALID_INTERVAL_METHOD =
            "<implementation.timer class='" + TestInvalidIntervalTimer.class.getName() + "' />";


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
        JavaImplementationProcessor processor = EasyMock.createMock(JavaImplementationProcessor.class);
        EasyMock.expect(processor.introspect(EasyMock.isA(String.class), EasyMock.eq(context))).andReturn(new InjectingComponentType());
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