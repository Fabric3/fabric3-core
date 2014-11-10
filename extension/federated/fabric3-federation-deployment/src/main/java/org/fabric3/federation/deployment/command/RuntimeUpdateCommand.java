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
package org.fabric3.federation.deployment.command;

import org.fabric3.spi.container.command.ResponseCommand;

/**
 * Sent by participant to receive deployment updates. The participant may send the update request to a controller or another zone member (typically
 * the zone leader).
 */
public class RuntimeUpdateCommand implements ResponseCommand {
    private static final long serialVersionUID = 1705187909349921487L;
    private String runtimeName;
    private String zoneName;
    private byte[] checksum;
    private RuntimeUpdateResponse response;

    public RuntimeUpdateCommand(String runtimeName, String zoneName, byte[] checksum) {
        this.runtimeName = runtimeName;
        this.zoneName = zoneName;
        this.checksum = checksum;
    }

    public String getRuntimeName() {
        return runtimeName;
    }

    public String getZoneName() {
        return zoneName;
    }

    public byte[] getChecksum() {
        return checksum;
    }

    public RuntimeUpdateResponse getResponse() {
        return response;
    }

    public void setResponse(RuntimeUpdateResponse response) {
        this.response = response;
    }
}