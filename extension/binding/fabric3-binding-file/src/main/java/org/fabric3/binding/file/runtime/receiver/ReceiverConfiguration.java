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
package org.fabric3.binding.file.runtime.receiver;

import java.io.File;
import java.util.regex.Pattern;

import org.fabric3.api.binding.file.ServiceAdapter;
import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.spi.container.wire.Interceptor;

/**
 * Configuration for instantiating a file binding receiver.
 */
public class ReceiverConfiguration {
    private String id;
    private File location;
    private Pattern filePattern;
    private File lockDirectory;
    Strategy strategy;
    File errorLocation;
    File archiveLocation;

    private long delay;

    private Interceptor interceptor;
    private ReceiverMonitor monitor;
    private ServiceAdapter adapter;

    public ReceiverConfiguration(String id,
                                 File location,
                                 String filePattern,
                                 Strategy strategy,
                                 File errorLocation,
                                 File archiveLocation,
                                 Interceptor interceptor,
                                 ServiceAdapter adapter,
                                 long delay,
                                 ReceiverMonitor monitor) {
        this.id = id;
        this.location = location;
        this.strategy = strategy;
        this.errorLocation = errorLocation;
        this.archiveLocation = archiveLocation;
        this.filePattern = Pattern.compile(filePattern);
        this.interceptor = interceptor;
        this.adapter = adapter;
        this.delay = delay;
        this.monitor = monitor;
        this.lockDirectory = new File(location, "locks");
    }

    public String getId() {
        return id;
    }

    public File getLocation() {
        return location;
    }

    public Pattern getFilePattern() {
        return filePattern;
    }

    public File getLockDirectory() {
        return lockDirectory;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public File getErrorLocation() {
        return errorLocation;
    }

    public File getArchiveLocation() {
        return archiveLocation;
    }

    public long getDelay() {
        return delay;
    }

    public Interceptor getInterceptor() {
        return interceptor;
    }

    public ServiceAdapter getAdapter() {
        return adapter;
    }

    public ReceiverMonitor getMonitor() {
        return monitor;
    }
}
