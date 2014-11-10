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
package org.fabric3.transport.ftp.server.ftplet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.transport.ftp.api.FtpLet;
import org.fabric3.transport.ftp.spi.FtpLetContainer;

/**
 * Default implementation of the FtpLet container.
 */
public class DefaultFtpLetContainer implements FtpLetContainer {

    private Map<String, FtpLet> ftpLets = new ConcurrentHashMap<>();

    public FtpLet getFtpLet(String fileName) {
        for (Map.Entry<String, FtpLet> entry : ftpLets.entrySet()) {
            if (fileName.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void registerFtpLet(String path, FtpLet ftpLet) {
        ftpLets.put(path, ftpLet);
    }

    public boolean isRegistered(String path) {
        return ftpLets.containsKey(path);
    }

}
