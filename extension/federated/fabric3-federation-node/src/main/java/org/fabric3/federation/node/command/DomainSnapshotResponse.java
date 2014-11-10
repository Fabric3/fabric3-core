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
package org.fabric3.federation.node.command;

import org.fabric3.spi.container.command.Response;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;

/**
 *
 */
public class DomainSnapshotResponse implements Response {
    private static final long serialVersionUID = -7851097480214771022L;
    private LogicalCompositeComponent snapshot;

    private String runtimeName;

    public DomainSnapshotResponse(LogicalCompositeComponent snapshot) {
        this.snapshot = snapshot;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public void setRuntimeName(String name) {
        runtimeName = name;
    }

    public LogicalCompositeComponent getSnapshot() {
        return snapshot;
    }
}
