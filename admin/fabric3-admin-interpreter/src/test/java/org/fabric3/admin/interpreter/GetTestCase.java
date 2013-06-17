/*
* Fabric3
* Copyright (c) 2009-2013 Metaform Systems
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
package org.fabric3.admin.interpreter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.admin.interpreter.communication.DomainConnection;
import org.fabric3.admin.interpreter.impl.InterpreterImpl;

/**
 *
 */
public class GetTestCase extends TestCase {

    public void testGet() throws Exception {
        DomainConnection domainConnection = EasyMock.createMock(DomainConnection.class);
        EasyMock.expect(domainConnection.createConnection(EasyMock.isA(String.class), EasyMock.eq("GET"))).andReturn(new MockConnection());
        EasyMock.replay(domainConnection);

        Interpreter interpreter = new InterpreterImpl(domainConnection);

        InputStream in = new ByteArrayInputStream("g foo \n quit".getBytes());
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        interpreter.processInteractive(in, out);

        EasyMock.verify(domainConnection);
    }

    public void testGetVerbose() throws Exception {
        DomainConnection domainConnection = EasyMock.createMock(DomainConnection.class);
        EasyMock.expect(domainConnection.createConnection(EasyMock.isA(String.class), EasyMock.eq("GET"))).andReturn(new MockConnection());
        EasyMock.replay(domainConnection);

        Interpreter interpreter = new InterpreterImpl(domainConnection);

        InputStream in = new ByteArrayInputStream("g foo -v \n quit".getBytes());
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        interpreter.processInteractive(in, out);

        EasyMock.verify(domainConnection);
    }

    public void testGetWithAuth() throws Exception {
        DomainConnection domainConnection = EasyMock.createMock(DomainConnection.class);
        domainConnection.setUsername("username");
        domainConnection.setPassword("password");
        EasyMock.expect(domainConnection.createConnection(EasyMock.isA(String.class), EasyMock.eq("GET"))).andReturn(new MockConnection());
        EasyMock.replay(domainConnection);

        Interpreter interpreter = new InterpreterImpl(domainConnection);

        InputStream in = new ByteArrayInputStream("g foo -u username -p password \n quit".getBytes());
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        interpreter.processInteractive(in, out);

        EasyMock.verify(domainConnection);
    }

    public void testGetVerboseWithAuth() throws Exception {
        DomainConnection domainConnection = EasyMock.createMock(DomainConnection.class);
        domainConnection.setUsername("username");
        domainConnection.setPassword("password");
        EasyMock.expect(domainConnection.createConnection(EasyMock.isA(String.class), EasyMock.eq("GET"))).andReturn(new MockConnection());
        EasyMock.replay(domainConnection);

        Interpreter interpreter = new InterpreterImpl(domainConnection);

        InputStream in = new ByteArrayInputStream("g foo -v -u username -p password \n quit".getBytes());
        PrintStream out = new PrintStream(new ByteArrayOutputStream());
        interpreter.processInteractive(in, out);

        EasyMock.verify(domainConnection);
    }

}