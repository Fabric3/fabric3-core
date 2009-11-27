/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.component;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class GroupInitializationExceptionTestCase extends TestCase {
    private PrintWriter writer;
    private PrintStream printer;
    private Exception cause1;
    private Exception cause2;
    private List<Exception> causes;
    private GroupInitializationException e;

    public void testCauses() {
        assertTrue(e.getCauses().contains(cause1));
        assertTrue(e.getCauses().contains(cause2));
    }

    // commented out to prevent confusing stack traces in the build log - uncomment to verify output
/*
    public void testPrintStackTraceToWriter() {
        e.printStackTrace(writer);
    }

    public void testPrintStackTraceToStream() {
        e.printStackTrace(printer);
    }

    public void testPrintStackTrace() {
        e.printStackTrace();
    }
*/

    protected void setUp() throws Exception {
        super.setUp();
        cause1 = new Exception("An Exception", new Exception("Nested Cause"));
        cause2 = new RuntimeException("A RuntimeException");
        causes = new ArrayList<Exception>();
        causes.add(cause1);
        causes.add(cause2);
        writer = new PrintWriter(System.err);
        printer = new PrintStream(System.err);
        e = new GroupInitializationException(causes);
    }
}
