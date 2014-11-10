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
package org.fabric3.binding.ftp.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.binding.ftp.common.Constants;
import org.fabric3.api.model.type.component.BindingDefinition;

/**
 * Binding definition loaded from the SCDL.
 */
public class FtpBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -889044951554792780L;

    private final TransferMode transferMode;
    private List<String> commands = new ArrayList<>();
    private String tmpFileSuffix;

    /**
     * Initializes the binding type.
     *
     * @param uri          Target URI.
     * @param transferMode the FTP transfer mode
     */
    public FtpBindingDefinition(URI uri, TransferMode transferMode) {
        super(uri, Constants.BINDING_QNAME);
        this.transferMode = transferMode;
    }

    /**
     * Gets the transfer mode.
     *
     * @return File transfer mode.
     */
    public TransferMode getTransferMode() {
        return transferMode;
    }

    /**
     * Gets the list of commands to execute before a STOR operation, i.e. a service invocation.
     *
     * @return the list of commands to execute before a STOR
     */
    public List<String> getSTORCommands() {
        return commands;
    }


    /**
     * Adds a command to execute before a put.
     *
     * @param command the command
     */
    public void addSTORCommand(String command) {
        commands.add(command);
    }

    /**
     * Gets the temporary file suffix to be used while file in transmission (i.e. during STOR operation).
     *
     * @return temporary file suffix
     */
    public String getTmpFileSuffix() {
        return tmpFileSuffix;
    }

    /**
     * Sets the temporary file suffix to be used while file in transmission (i.e. during STOR operation).
     *
     * @param tmpFileSuffix temporary file suffix
     */
    public void setTmpFileSuffix(String tmpFileSuffix) {
        this.tmpFileSuffix = tmpFileSuffix;
    }
}
