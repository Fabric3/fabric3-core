package org.fabric3.monitor.runtime;

import java.io.StringWriter;
import javax.xml.bind.JAXBContext;

import junit.framework.TestCase;

import org.fabric3.api.annotation.monitor.MonitorLevel;

/**
 *
 */
public class MonitorEventImplTestCase extends TestCase {

    public void testMarshal() throws Exception {
        JAXBContext context = JAXBContext.newInstance(MonitorEventImpl.class);
        MonitorEventImpl event = new MonitorEventImpl("runtime", "source", MonitorLevel.SEVERE, 10000, "thread", "message");
        StringWriter writer = new StringWriter();
        context.createMarshaller().marshal(event, writer);
    }
}
