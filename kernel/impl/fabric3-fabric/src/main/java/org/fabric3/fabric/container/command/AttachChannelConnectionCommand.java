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
 */
package org.fabric3.fabric.container.command;

import org.fabric3.spi.model.physical.PhysicalChannelConnection;

/**
 * Used to establish an event channel connection.
 */
public class AttachChannelConnectionCommand implements Command {
    private PhysicalChannelConnection connection;

    public AttachChannelConnectionCommand(PhysicalChannelConnection connection) {
        this.connection = connection;
    }

    public PhysicalChannelConnection getConnection() {
        return connection;
    }
}