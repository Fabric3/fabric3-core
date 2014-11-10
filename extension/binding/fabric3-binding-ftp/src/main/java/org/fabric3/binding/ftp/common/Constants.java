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
package org.fabric3.binding.ftp.common;

import javax.xml.namespace.QName;

/**
 *
 */
public interface Constants {

    /**
     * Qualified name for the binding element.
     */
    QName BINDING_QNAME = new QName(org.fabric3.api.Namespaces.F3, "binding.ftp");

    /**
     * Qualified name for the policy element.
     */
    QName POLICY_QNAME = new QName(org.fabric3.api.Namespaces.F3, "security");

    /**
     * The value for specifying no timeout for blocking operations on an FTP socket.
     */
    int NO_TIMEOUT = 0;
}
