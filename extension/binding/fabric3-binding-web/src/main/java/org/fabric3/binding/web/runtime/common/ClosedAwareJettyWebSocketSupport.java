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
package org.fabric3.binding.web.runtime.common;

import java.io.IOException;

import org.atmosphere.websocket.JettyWebSocketSupport;

import static org.eclipse.jetty.websocket.WebSocket.Outbound;

/**
 * Overrides <code>JettyWebSocketSupport</code> behavior to throw an unchecked exception if the underlying websocket connection is closed. This allows
 * the upstream broadcaster to catch the exception and remove the connection from its collection of active connections.
 * <p/>
 * Note that attempting to write to a closed socket will result in an <code>IOException</code> being thrown. However, these exceptions are trapped and
 * logged by callers of this class instead of being re-thrown, which results in the upstream broadcaster maintaining the connection its is active
 * collection.
 *
 * @version $Rev $ $Date
 */
public class ClosedAwareJettyWebSocketSupport extends JettyWebSocketSupport {
    private Outbound socket;

    public ClosedAwareJettyWebSocketSupport(Outbound outbound) {
        super(outbound);
        this.socket = outbound;
    }

    @Override
    public void write(byte frame, String data) throws IOException {
        checkClosed();
        super.write(frame, data);
    }

    @Override
    public void write(byte frame, byte[] data) throws IOException {
        checkClosed();
        super.write(frame, data);
    }

    @Override
    public void write(byte frame, byte[] data, int offset, int length) throws IOException {
        checkClosed();
        super.write(frame, data, offset, length);
    }

    private void checkClosed() {
        if (!socket.isOpen()) {
            throw new SocketClosedException("Socket is closed");
        }
    }


}