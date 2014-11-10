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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.fabric.container.command;

/**
 * A command to attach a set of wires from a source component to a set of targets on a runtime. Valid targets can be a service offered by another
 * component, a binding transport, or a resource.
 */
public class AttachWireCommand extends WireCommand {
    private static final long serialVersionUID = -5157427289507028318L;

    public DetachWireCommand getCompensatingCommand() {
        DetachWireCommand compensating = new DetachWireCommand();
        compensating.setPhysicalWireDefinition(getPhysicalWireDefinition());
        return compensating;
    }
}
