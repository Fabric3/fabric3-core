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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;

/**
 * Overrides the Jetty <code>StatisticsHandler</code> to provide the ability to start and stop statistics collection at runtime. Also provides a
 * custom management view.
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