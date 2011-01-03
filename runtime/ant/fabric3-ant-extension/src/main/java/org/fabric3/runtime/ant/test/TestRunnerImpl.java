/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.runtime.ant.test;

import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.runtime.ant.api.TestRunner;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.fabric3.test.spi.TestWireHolder;

/**
 * @version $Rev$ $Date$
 */
public class TestRunnerImpl implements TestRunner {
    private TestWireHolder holder;
    private TestRunnerMonitor monitor;

    public TestRunnerImpl(@Reference TestWireHolder holder, @Monitor TestRunnerMonitor monitor) {
        this.holder = holder;
        this.monitor = monitor;
    }

    public void executeTests() {
        Map<String, Wire> wires = holder.getWires();
        if (wires.isEmpty()) {
            monitor.noTests();
            return;
        }
        int count = 0;
        for (Map.Entry<String, Wire> entry : wires.entrySet()) {
            String name = entry.getKey();
            Wire wire = entry.getValue();
            execute(name, wire);
            count = count + wire.getInvocationChains().size();
        }
        monitor.finished(count);
    }

    public void execute(String name, Wire wire) {
        monitor.runningTest(name);
        for (InvocationChain chain : wire.getInvocationChains()) {
            String operationName = chain.getPhysicalOperation().getName();
            WorkContext workContext = new WorkContext();
            CallFrame frame = new CallFrame();
            workContext.addCallFrame(frame);

            MessageImpl msg = new MessageImpl();
            msg.setWorkContext(workContext);
            long start = System.currentTimeMillis();
            Message response = chain.getHeadInterceptor().invoke(msg);
            long elapsed = (System.currentTimeMillis() - start) / 1000;
            if (response.isFault()) {
                Throwable exception = (Throwable) response.getBody();
                monitor.failed(operationName, exception);
                exception.printStackTrace();
            } else {
                monitor.passed(operationName, elapsed);
            }

        }
    }

}
