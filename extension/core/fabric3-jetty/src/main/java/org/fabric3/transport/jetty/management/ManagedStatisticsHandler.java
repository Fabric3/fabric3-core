/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.StatisticsHandler;

import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * Overrides the Jetty <code>StatisticsHandler</code> to provide the ability to start and stop statistics collection at runtime. Also provides a
 * custom management view.
 *
 * @version $Rev: 9172 $ $Date: 2010-06-30 16:49:34 +0200 (Wed, 30 Jun 2010) $
 */
@Management
public class ManagedStatisticsHandler extends StatisticsHandler {
    private AtomicBoolean enabled = new AtomicBoolean();

    private AtomicLong statsStartedAt = new AtomicLong();
    private AtomicLong statsEndedAt = new AtomicLong(-1);

    @Override
    public void handle(String path, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse)
            throws IOException, ServletException {
        if (enabled.get()) {
            super.handle(path, request, httpRequest, httpResponse);
        } else {
            if (_handler != null && isStarted()) {
                _handler.handle(path, request, httpRequest, httpResponse);
            }
        }
    }

    @Override
    public void statsReset() {
        statsStartedAt.set(System.currentTimeMillis());
        statsEndedAt.set(-1);
        super.statsReset();
    }

    @ManagementOperation(description = "Starts statistics collection")
    public void startStatisticsCollection() {
        statsReset();
        enabled.set(true);
    }

    @ManagementOperation(description = "Suspends statistics collection")
    public void stopStatisticsCollection() {
        enabled.set(false);
    }

    @ManagementOperation(description = "The number of requests handled by this handler since, excluding active requests")
    public int getRequests() {
        return super.getRequests();
    }

    @ManagementOperation(description = "The number of requests currently active")
    public int getRequestsActive() {
        return super.getRequestsActive();
    }

    @ManagementOperation(description = "The maximum number of active requests ")
    public int getRequestsActiveMax() {
        return super.getRequestsActiveMax();
    }

    @ManagementOperation(description = "The maximum time (in milliseconds) of request handling")
    public long getRequestTimeMax() {
        return super.getRequestTimeMax();
    }

    @ManagementOperation(description = "The total time (in milliseconds) of requests handling")
    public long getRequestTimeTotal() {
        return super.getRequestTimeTotal();
    }

    @ManagementOperation(description = "The mean time (in milliseconds) of request handling")
    public double getRequestTimeMean() {
        return super.getRequestTimeMean();
    }

    @ManagementOperation(description = "The standard deviation of time (in milliseconds) of request handling")
    public double getRequestTimeStdDev() {
        return super.getRequestTimeStdDev();
    }

    @ManagementOperation(description = "The number of dispatches seen by this handler")
    public int getDispatched() {
        return super.getDispatched();
    }

    @ManagementOperation(description = "The number of dispatches currently in this handler")
    public int getDispatchedActive() {
        return super.getDispatchedActive();
    }

    @ManagementOperation(description = "The max number of dispatches currently in this handler ")
    public int getDispatchedActiveMax() {
        return super.getDispatchedActiveMax();
    }

    @ManagementOperation(description = "The maximum time (in milliseconds) of request dispatch ")
    public long getDispatchedTimeMax() {
        return super.getDispatchedTimeMax();
    }

    @ManagementOperation(description = "The total time (in milliseconds) of requests handling ")
    public long getDispatchedTimeTotal() {
        return super.getDispatchedTimeTotal();
    }

    @ManagementOperation(description = "The mean time (in milliseconds) of request handling")
    public double getDispatchedTimeMean() {
        return super.getDispatchedTimeMean();
    }

    @ManagementOperation(description = "The standard deviation of time (in milliseconds) of request handling")
    public double getDispatchedTimeStdDev() {
        return super.getDispatchedTimeStdDev();
    }

    @ManagementOperation(description = "The number of requests handled by this handler")
    public int getSuspends() {
        return super.getSuspends();
    }

    @ManagementOperation(description = "The number of requests currently suspended")
    public int getSuspendsActive() {
        return super.getSuspendsActive();
    }

    @ManagementOperation(description = "The maximum number of current suspended requests")
    public int getSuspendsActiveMax() {
        return super.getSuspendsActiveMax();
    }

    @ManagementOperation(description = "The number of requests that have been resumed")
    public int getResumes() {
        return super.getResumes();
    }

    @ManagementOperation(description = "The number of requests that expired while suspended")
    public int getExpires() {
        return super.getExpires();
    }

    @ManagementOperation(description = "The number of responses with a 1xx status")
    public int getResponses1xx() {
        return super.getResponses1xx();
    }

    @ManagementOperation(description = "The number of responses with a 2xx status")
    public int getResponses2xx() {
        return super.getResponses2xx();
    }

    @ManagementOperation(description = "The number of responses with a 3xx status")
    public int getResponses3xx() {
        return super.getResponses3xx();
    }

    @ManagementOperation(description = "The number of responses with a 4xx status")
    public int getResponses4xx() {
        return super.getResponses4xx();
    }

    @ManagementOperation(description = "The number of responses with a 5xx status")
    public int getResponses5xx() {
        return super.getResponses5xx();
    }

    @ManagementOperation(description = "The statistics sampling period in milliseconds")
    public long getStatsOnMs() {
        long ended = statsEndedAt.get();
        if (ended != -1) {
            return ended - statsStartedAt.get();
        }
        return System.currentTimeMillis() - statsStartedAt.get();
    }

    @ManagementOperation(description = "The total bytes of content sent in responses")
    public long getResponsesBytesTotal() {
        return super.getResponsesBytesTotal();
    }


}