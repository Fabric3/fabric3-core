/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.transport.jetty.management;

import org.eclipse.jetty.server.session.HashSessionManager;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * Overrides the Jetty <code>HashSessionManager</code> to provide a custom management view.
 *
 * @version $Rev: 9172 $ $Date: 2010-06-30 16:49:34 +0200 (Wed, 30 Jun 2010) $
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
    @ManagementOperation(description = "Invalidates all live sessions")
    protected void invalidateSessions() {
        super.invalidateSessions();
    }

    @Override
    @ManagementOperation(description = "The maximum session cookie age in seconds")
    public int getMaxCookieAge() {
        return super.getMaxCookieAge();
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
    @ManagementOperation(description = "True if sessions are using client-side cookies")
    public void setUsingCookies(boolean usingCookies) {
        super.setUsingCookies(usingCookies);
    }

    @Override
    @ManagementOperation(description = "Maximum cookie age")
    public void setMaxCookieAge(int maxCookieAgeInSeconds) {
        super.setMaxCookieAge(maxCookieAgeInSeconds);
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