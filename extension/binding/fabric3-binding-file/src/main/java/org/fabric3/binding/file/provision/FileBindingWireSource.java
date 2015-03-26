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
package org.fabric3.binding.file.provision;

import javax.activation.DataHandler;
import java.net.URI;

import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.spi.model.physical.PhysicalWireSource;

/**
 * Generated metadata used for attaching a service to a file listener.
 */
public class FileBindingWireSource extends PhysicalWireSource {
    private String location;
    private Strategy strategy;
    private String archiveLocation;
    private String errorLocation;
    private String adapterClass;
    private URI adapterUri;
    private String pattern;
    private long delay;
    private boolean dataHandler;

    public FileBindingWireSource(URI uri,
                                 String pattern,
                                 String location,
                                 Strategy strategy,
                                 String archiveLocation,
                                 String errorLocation,
                                 String adapterClass,
                                 URI adapterUri,
                                 long delay,
                                 boolean dataHandler) {
        this.pattern = pattern;
        this.location = location;
        this.strategy = strategy;
        this.archiveLocation = archiveLocation;
        this.errorLocation = errorLocation;
        this.adapterClass = adapterClass;
        this.adapterUri = adapterUri;
        this.delay = delay;
        this.dataHandler = dataHandler;
        setUri(uri);
    }

    /**
     * Returns the pattern to match files on.
     *
     * @return the pattern to match files on
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * The directory to receive files in. May be relative or absolute. If it is relative, it will be resolved against the runtime data directory.
     *
     * @return the directory to receive files in
     */
    public String getLocation() {
        return location;
    }

    /**
     * Whether to archive or delete received files
     *
     * @return whether to archive or delete received files
     */
    public Strategy getStrategy() {
        return strategy;
    }

    /**
     * The directory to archive files to. May be relative or absolute. If it is relative, it will be resolved against the runtime data directory.
     *
     * @return the directory to archive files to
     */
    public String getArchiveLocation() {
        return archiveLocation;
    }

    /**
     * The directory to place invalid files in. May be relative or absolute. If it is relative, it will be resolved against the runtime data directory
     *
     * @return the directory to place invalid files in
     */
    public String getErrorLocation() {
        return errorLocation;
    }

    /**
     * The adapter class for processing received files.
     *
     * @return the adapter class for processing received files or null
     */
    public String getAdapterClass() {
        return adapterClass;
    }

    /**
     * Returns the URI of the adaptor component for receiving files.
     *
     * @return the URI of the adaptor component for receiving files or null
     */
    public URI getAdapterUri() {
        return adapterUri;
    }

    /**
     * The delay in milliseconds between directory scans.
     *
     * @return he delay in milliseconds between directory scans
     */
    public long getDelay() {
        return delay;
    }

    /**
     * Returns true if the contract takes the Java Activation Framework {@link DataHandler} type as a parameter.
     *
     * @return true if the contract takes the Java Activation Framework {@link DataHandler} type as a parameter
     */
    public boolean isDataHandler() {
        return dataHandler;
    }
}
