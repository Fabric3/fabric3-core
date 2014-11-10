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
package org.fabric3.federation.deployment.coordinator;

import java.util.LinkedList;

import org.oasisopen.sca.annotation.Property;

import org.fabric3.federation.deployment.command.DeploymentCommand;

/**
 *
 */
public class DeploymentCacheImpl implements DeploymentCache {
    private LinkedList<DeploymentCommand> history = new LinkedList<>();
    private int threshold = 3;

    public synchronized void cache(DeploymentCommand command) {
        if (history.size() >= threshold) {
            history.removeFirst();
        }
        history.add(command);
    }

    @Property(required = false)
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public synchronized DeploymentCommand undo() {
        if (history.isEmpty()) {
            return null;
        }
        return history.removeLast();
    }

    public synchronized DeploymentCommand get() {
        if (history.isEmpty()) {
            return null;
        }
        return history.getLast();
    }

}
