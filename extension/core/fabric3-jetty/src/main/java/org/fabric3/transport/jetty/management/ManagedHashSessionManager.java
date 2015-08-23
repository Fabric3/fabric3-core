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
package org.fabric3.transport.jetty.management;

import org.eclipse.jetty.server.session.HashSessionManager;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * Overrides the Jetty <code>HashSessionManager</code> to provide a custom management view.
 */
@Management
public class ManagedHashSessionManager extends HashSessionManager {

    @Override
    @ManagementOperation(description = "The session manager state")
    public String getState() {
        return super.getState();
    }

    @Override
    @ManagementOperation(description = "The max period of inactivity after which a session is invalidated in seconds")
    public int getMaxInactiveInterval() {
        return super.getMaxInactiveInterval();
    }

    @Override
    @ManagementOperation(description = "The max period of inactivity after which a session is invalidated in seconds")
    public void setMaxInactiveInterval(int seconds) {
        super.setMaxInactiveInterval(seconds);
    }

    @Override
    @ManagementOperation(description = "Session scavenger period in seconds")
    public int getScavengePeriod() {
        return super.getScavengePeriod();
    }

    @Override
    @ManagementOperation(description = "Session scavenger period in seconds")
    public void setScavengePeriod(int seconds) {
        super.setScavengePeriod(seconds);
    }

    @Override
    @ManagementOperation(description = "The maximum session cookie age in seconds")
    public int getSessionsMax() {
        return super.getSessionsMax();
    }

    @Override
    @ManagementOperation(description = "The total number of active sessions")
    public int getSessionsTotal() {
        return super.getSessionsTotal();
    }

    @Override
    @ManagementOperation(description = "The length of time after which a cookie should be refreshed in seconds")
    public int getRefreshCookieAge() {
        return super.getRefreshCookieAge();
    }

    @Override
    @ManagementOperation(description = "The length of time after which a cookie should be refreshed in seconds")
    public void setRefreshCookieAge(int ageInSeconds) {
        super.setRefreshCookieAge(ageInSeconds);
    }

    @Override
    @ManagementOperation(description = "True if sessions are using client-side cookies")
    public boolean isUsingCookies() {
        return super.isUsingCookies();
    }

    @Override
    @ManagementOperation(description = "The maximum amount of time session remained valid")
    public long getSessionTimeMax() {
        return super.getSessionTimeMax();
    }

    @Override
    @ManagementOperation(description = "The total amount of time all sessions remained valid")
    public long getSessionTimeTotal() {
        return super.getSessionTimeTotal();
    }

    @Override
    @ManagementOperation(description = "The average amount of time sessions remained valid")
    public double getSessionTimeMean() {
        return super.getSessionTimeMean();
    }

    @Override
    @ManagementOperation(description = "The standard deviation of the amount of time sessions remained valid")
    public double getSessionTimeStdDev() {
        return super.getSessionTimeStdDev();
    }

}