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
package org.fabric3.federation.provisioning;

import java.net.URL;

import org.fabric3.api.annotation.monitor.Debug;
import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;
import org.fabric3.api.annotation.monitor.Warning;
import org.fabric3.spi.security.AuthenticationException;
import org.fabric3.spi.security.AuthorizationException;

/**
 *
 */
public interface ProvisionMonitor {

    @Severe
    void errorMessage(String message);

    @Severe
    void error(String message, Throwable e);

    @Severe("HTTPS must be enabled for secure provisioning")
    void httpsNotEnabled();

    @Warning("Username not set for secure contribution provisioning")
    void warnUsername();

    @Warning("Password not set for secure contribution provisioning")
    void warnPassword();

    @Info("Invalid authentication received when attempting to provision a contribution")
    void badAuthentication(AuthenticationException e);

    @Info("Invalid authorization received when attempting to provision a contribution")
    void badAuthorization(AuthorizationException e);

    @Debug("Resolving contribution {0}")
    void resolving(URL url);

}
