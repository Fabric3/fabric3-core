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
 *
 */
package org.fabric3.fabric.host;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import org.fabric3.spi.host.Port;

/**
 *
 */
public class PortImpl implements Port, Serializable {
    private static final long serialVersionUID = -708646870372177434L;
    private String name;
    private int number;
    private transient ServerSocket serverSocket;
    private transient DatagramSocket datagramSocket;

    public PortImpl(String name, int number, ServerSocket serverSocket, DatagramSocket datagramSocket) {
        this.name = name;
        this.number = number;
        this.serverSocket = serverSocket;
        this.datagramSocket = datagramSocket;
    }

    public String getName() {
        return name;
    }

    public int getNumber() {
        return number;
    }

    public void bind(TYPE type) {
        if (TYPE.TCP == type) {
            try {
                if (serverSocket == null || serverSocket.isClosed()) {   // socket will be null if this class is serialized
                    return;
                }
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (datagramSocket == null || datagramSocket.isClosed()) {   // socket will be null if this class is serialized
                return;
            }
            datagramSocket.close();
            datagramSocket = null;
        }
    }

    public void release() {
        bind(TYPE.TCP);
        bind(TYPE.UDP);
    }

}


