/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.host;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import org.fabric3.spi.host.Port;

/**
 *
 */
public class PortImpl implements Port {
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


