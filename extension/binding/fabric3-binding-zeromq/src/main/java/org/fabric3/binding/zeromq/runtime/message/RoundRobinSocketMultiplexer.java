/*
 * Fabric3 Copyright (c) 2009-2011 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.zeromq.runtime.message;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CopyOnWriteArrayList;

import org.zeromq.ZMQ;

/**
 * Implements a round-robin strategy for selecting a next available socket from a collection of sockets.
 *
 * @version $Revision: 10212 $ $Date: 2011-03-15 18:20:58 +0100 (Tue, 15 Mar 2011) $
 */
public class RoundRobinSocketMultiplexer implements SocketMultiplexer {
    private List<ZMQ.Socket> sockets = new CopyOnWriteArrayList<ZMQ.Socket>();
    private Iterator<ZMQ.Socket> iterator;

    public ZMQ.Socket get() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return iterator.next();
    }

    public void update(List<ZMQ.Socket> sockets) {
        this.sockets = sockets;
        if (sockets.size() == 1) {
            iterator = new SingletonIterator(sockets.get(0));
        } else {
            iterator = sockets.iterator();
        }
    }

    public void close() {
        for (ZMQ.Socket socket : sockets) {
            socket.close();
        }

    }

    private boolean hasNext() {
        if (!iterator.hasNext()) {
            // return to top of list
            iterator = sockets.iterator();
        }
        return iterator.hasNext();
    }

    private class SingletonIterator implements Iterator<ZMQ.Socket> {
        private ZMQ.Socket socket;

        private SingletonIterator(ZMQ.Socket socket) {
            this.socket = socket;
        }

        public boolean hasNext() {
            return true;
        }

        public ZMQ.Socket next() {
            return socket;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }


}
