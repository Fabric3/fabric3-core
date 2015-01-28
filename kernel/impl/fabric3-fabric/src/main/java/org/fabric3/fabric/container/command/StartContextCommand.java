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

import javax.xml.namespace.QName;

import org.fabric3.spi.container.command.Command;

/**
 * Starts a composite context on a runtime.
 */
public class StartContextCommand implements Command {
    private static final long serialVersionUID = -2132991925467598257L;

    private QName deployable;
    private boolean log;

    public StartContextCommand(QName deployable, boolean log) {
        this.deployable = deployable;
        this.log = log;
    }

    public QName getDeployable() {
        return deployable;
    }

    public boolean isLog() {
        return log;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StartContextCommand that = (StartContextCommand) o;

        return !(deployable != null ? !deployable.equals(that.deployable) : that.deployable != null);

    }

    public int hashCode() {
        return (deployable != null ? deployable.hashCode() : 0);
    }
}
