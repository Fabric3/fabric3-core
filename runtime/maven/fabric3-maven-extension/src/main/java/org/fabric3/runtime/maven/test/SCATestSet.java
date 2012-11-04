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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.runtime.maven.test;

import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.report.StackTraceWriter;
import org.apache.maven.surefire.testset.SurefireTestSet;
import org.apache.maven.surefire.testset.TestSetFailedException;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 *
 */
public class SCATestSet implements SurefireTestSet {
    private final String name;
    private Wire wire;

    public SCATestSet(String name, Wire wire) {
        this.name = name;
        this.wire = wire;
    }

    public void execute(ReporterManager reporterManager, ClassLoader loader) throws TestSetFailedException {
        for (InvocationChain chain : wire.getInvocationChains()) {
            String operationName = chain.getPhysicalOperation().getName();
            reporterManager.testStarting(new ReportEntry(this, operationName, name));
            try {
                WorkContext workContext = new WorkContext();
                CallFrame frame = new CallFrame();
                workContext.addCallFrame(frame);

                MessageImpl msg = new MessageImpl();
                msg.setWorkContext(workContext);
                Message response = chain.getHeadInterceptor().invoke(msg);
                if (response.isFault()) {
                    throw new TestSetFailedException(operationName, (Throwable) response.getBody());
                }

                reporterManager.testSucceeded(new ReportEntry(this, operationName, name));

            } catch (TestSetFailedException e) {
                StackTraceWriter stw = new PojoStackTraceWriter(name, operationName, e.getCause());
                reporterManager.testFailed(new ReportEntry(this, operationName, name, stw));
                throw e;
            }
        }
    }

    public int getTestCount() {
        return wire.getInvocationChains().size();
    }

    public String getName() {
        return name;
    }

    public Class<?> getTestClass() {
        throw new UnsupportedOperationException();
    }
}
