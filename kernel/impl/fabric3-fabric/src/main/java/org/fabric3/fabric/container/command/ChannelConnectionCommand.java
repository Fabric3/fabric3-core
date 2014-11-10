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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.fabric3.spi.container.command.CompensatableCommand;

/**
 * Used to establish or dispose a channel connection. This may include provisioning the channel (or disposing it) to a runtime where the producer or consumer is
 * hosted.
 */
public class ChannelConnectionCommand implements CompensatableCommand {
    private static final long serialVersionUID = 8746788639966402901L;

    private List<BuildChannelCommand> buildCommands;
    private List<DisposeChannelCommand> disposeCommands;

    private List<AttachChannelConnectionCommand> attachCommands;
    private List<DetachChannelConnectionCommand> detachCommands;

    public ChannelConnectionCommand() {
        attachCommands = new ArrayList<>();
        detachCommands = new ArrayList<>();
        buildCommands = new ArrayList<>();
        disposeCommands = new ArrayList<>();
    }

    public ChannelConnectionCommand getCompensatingCommand() {
        // return the commands in reverse order
        ChannelConnectionCommand compensating = new ChannelConnectionCommand();

        for (BuildChannelCommand command : buildCommands) {
            compensating.addDisposeChannelCommand(command.getCompensatingCommand());
        }
        for (DisposeChannelCommand command : disposeCommands) {
            compensating.addBuildChannelCommand(command.getCompensatingCommand());
        }
        if (!attachCommands.isEmpty()) {
            ListIterator<AttachChannelConnectionCommand> iterator = attachCommands.listIterator(attachCommands.size());
            while (iterator.hasPrevious()) {
                AttachChannelConnectionCommand command = iterator.previous();
                DetachChannelConnectionCommand compensatingCommand = command.getCompensatingCommand();
                compensating.add(compensatingCommand);
            }
        }
        if (!detachCommands.isEmpty()) {
            ListIterator<DetachChannelConnectionCommand> iterator = detachCommands.listIterator(detachCommands.size());
            while (iterator.hasPrevious()) {
                DetachChannelConnectionCommand command = iterator.previous();
                AttachChannelConnectionCommand compensatingCommand = command.getCompensatingCommand();
                compensating.add(compensatingCommand);
            }
        }
        return compensating;
    }

    public List<BuildChannelCommand> getBuildChannelCommands() {
        return buildCommands;
    }

    public void addBuildChannelCommand(BuildChannelCommand command) {
        this.buildCommands.add(command);
    }

    public List<DisposeChannelCommand> getDisposeChannelCommands() {
        return disposeCommands;
    }

    public void addDisposeChannelCommand(DisposeChannelCommand command) {
        this.disposeCommands.add(command);
    }

    public List<AttachChannelConnectionCommand> getAttachCommands() {
        return attachCommands;
    }

    public void add(AttachChannelConnectionCommand command) {
        attachCommands.add(command);
    }

    public List<DetachChannelConnectionCommand> getDetachCommands() {
        return detachCommands;
    }

    public void add(DetachChannelConnectionCommand command) {
        detachCommands.add(command);
    }
}
