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
package org.fabric3.fabric.container.command;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains commands for attaching and detaching wires for a component.
 */
public class ConnectionCommand implements Command {
    private URI componentUri;
    private List<AttachWireCommand> attachCommands;
    private List<DetachWireCommand> detachCommands;

    public ConnectionCommand(URI componentUri) {
        this();
        this.componentUri = componentUri;
    }

    protected ConnectionCommand() {
        attachCommands = new ArrayList<>();
        detachCommands = new ArrayList<>();
    }

    public URI getComponentUri() {
        return componentUri;
    }

    public List<AttachWireCommand> getAttachCommands() {
        return attachCommands;
    }

    public List<DetachWireCommand> getDetachCommands() {
        return detachCommands;
    }

    public void add(AttachWireCommand command) {
        attachCommands.add(command);
    }

    public void add(DetachWireCommand command) {
        detachCommands.add(command);
    }

}
