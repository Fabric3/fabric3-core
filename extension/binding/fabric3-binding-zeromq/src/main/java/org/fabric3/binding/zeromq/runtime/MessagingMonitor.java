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
package org.fabric3.binding.zeromq.runtime;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

/**
 *
 */
public interface MessagingMonitor {

    @Severe
    public void error(Throwable t);

    @Severe
    void error(String message);

    @Severe
    public void warn(String message);

    @Info("Provisioned ZeroMQ subscriber [{0}]")
    void onSubscribe(String id);

    @Info("Removed ZeroMQ subscriber [{0}]")
    void onUnsubscribe(String id);

    @Info("Provisioned ZeroMQ endpoint [{0}]")
    void onProvisionEndpoint(String id);

    @Info("Removed ZeroMQ endpoint [{0}]")
    void onRemoveEndpoint(String id);

    @Debug("ZeroMQ message dropped due to unavailable endpoint")
    void dropMessage();

}
