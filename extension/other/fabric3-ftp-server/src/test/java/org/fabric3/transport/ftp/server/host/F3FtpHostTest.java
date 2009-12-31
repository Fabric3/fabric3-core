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
*/
package org.fabric3.transport.ftp.server.host;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import org.apache.commons.net.ftp.FTPClient;

import org.fabric3.transport.ftp.server.codec.CodecFactory;
import org.fabric3.transport.ftp.server.ftplet.DefaultFtpLetContainer;
import org.fabric3.transport.ftp.server.handler.PassRequestHandler;
import org.fabric3.transport.ftp.server.handler.PasvRequestHandler;
import org.fabric3.transport.ftp.server.handler.StorRequestHandler;
import org.fabric3.transport.ftp.server.handler.UserRequestHandler;
import org.fabric3.transport.ftp.server.monitor.FtpMonitor;
import org.fabric3.transport.ftp.server.passive.PassiveConnectionServiceImpl;
import org.fabric3.transport.ftp.server.protocol.RequestHandler;
import org.fabric3.transport.ftp.server.security.FileSystemUserManager;
import org.fabric3.transport.ftp.spi.FtpLetContainer;

/**
 * @version $Rev$ $Date$
 */
public class F3FtpHostTest extends TestCase {

    private F3FtpHost ftpHost;

    public void setUp() throws Exception {

        FtpMonitor ftpMonitor = new TestFtpMonitor();

        Map<String, RequestHandler> requestHandlers = new HashMap<String, RequestHandler>();

        Map<String, String> users = new HashMap<String, String>();
        users.put("user", "password");
        FileSystemUserManager userManager = new FileSystemUserManager();
        userManager.setUsers(users);
        requestHandlers.put("USER", new UserRequestHandler());

        PassRequestHandler passCommandHandler = new PassRequestHandler();
        passCommandHandler.setUserManager(userManager);
        requestHandlers.put("PASS", passCommandHandler);

        PassiveConnectionServiceImpl passiveConnectionService = new PassiveConnectionServiceImpl();
        passiveConnectionService.setMinPort(50000);
        passiveConnectionService.setMaxPort(60000);
        passiveConnectionService.init();
        PasvRequestHandler pasvRequestHandler = new PasvRequestHandler();
        pasvRequestHandler.setPassivePortService(passiveConnectionService);
        requestHandlers.put("PASV", pasvRequestHandler);

        StorRequestHandler storRequestHandler = new StorRequestHandler();
        storRequestHandler.setPassivePortService(passiveConnectionService);
        storRequestHandler.setFtpMonitor(ftpMonitor);
        FtpLetContainer ftpLetContainer = new DefaultFtpLetContainer();
        ftpLetContainer.registerFtpLet("/", new DummyFtpLet());
        storRequestHandler.setFtpLetContainer(ftpLetContainer);
        requestHandlers.put("STOR", storRequestHandler);

        ftpHost = new F3FtpHost();

        FtpHandler ftpHandler = new FtpHandler();
        ftpHandler.setRequestHandlers(requestHandlers);
        ftpHandler.setFtpMonitor(ftpMonitor);

        ftpHost.setFtpHandler(ftpHandler);
        ftpHost.setCommandPort(1234);
        ftpHost.setCodecFactory(new CodecFactory());
        ftpHost.start();

    }

    public void tearDown() throws Exception {
        ftpHost.stop();
    }

    public void testValidLogin() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(InetAddress.getLocalHost(), 1234);
        ftpClient.user("user");
        assertEquals(230, ftpClient.pass("password"));
    }

    public void testInvalidLogin() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(InetAddress.getLocalHost(), 1234);
        ftpClient.user("user");
        assertEquals(530, ftpClient.pass("password1"));
    }

    public void testStor() throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(InetAddress.getLocalHost(), 1234);
        ftpClient.user("user");
        ftpClient.pass("password");
        ftpClient.enterLocalPassiveMode();
        ftpClient.storeFile("/resource/test.dat", new ByteArrayInputStream("TEST\r\n".getBytes()));
    }

}
