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
package org.fabric3.api.binding.file.model;

import org.fabric3.api.binding.file.annotation.Strategy;
import org.fabric3.api.model.type.component.BindingDefinition;

/**
 * A file binding configuration set on a reference.
 */
public class FileBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -8904535030035183877L;
    private static final String BINDING_FILE = "file";

    private String pattern;
    private String location;
    private Strategy strategy = Strategy.DELETE;
    private String archiveLocation;
    private String errorLocation;
    private String adapterClass;
    private String adapterUri;
    private long delay;

    /**
     * Constructor.
     *
     * @param name the binding name
     */
    public FileBindingDefinition(String name) {
        super(name, null, BINDING_FILE);
    }

    /**
     * Constructor.
     *
     * @param name          the binding name
     * @param location      the directory to receive files in. May be relative or absolute. If it is relative, it will be resolved against the runtime data
     *                      directory.
     * @param errorLocation the directory to place invalid files in. May be relative or absolute. If it is relative, it will be resolved against the runtime
     *                      data directory.
     */
    public FileBindingDefinition(String name, String location, String errorLocation) {
        super(name, null, BINDING_FILE);
        this.location = location;
        this.errorLocation = errorLocation;
    }

    /**
     * Constructor.
     *
     * @param name            the binding name
     * @param pattern         the pattern to match files on. May be null.
     * @param location        the directory to receive files in. May be relative or absolute. If it is relative, it will be resolved against the runtime data
     *                        directory.
     * @param strategy        whether to archive or delete received files
     * @param archiveLocation the directory to archive files to. May be relative or absolute. If it is relative, it will be resolved against the runtime data
     *                        directory.
     * @param errorLocation   the directory to place invalid files in. May be relative or absolute. If it is relative, it will be resolved against the runtime
     *                        data directory.
     * @param adapterClass    the adapter class for processing received files. May be null.
     * @param adapterUri      the URI of the adaptor component for receiving files. May be null.
     * @param delay           the delay in milliseconds between directory scans
     */
    public FileBindingDefinition(String name,
                                 String pattern,
                                 String location,
                                 Strategy strategy,
                                 String archiveLocation,
                                 String errorLocation,
                                 String adapterClass,
                                 String adapterUri,
                                 long delay) {
        super(name, null, BINDING_FILE);
        this.pattern = pattern;
        this.location = location;
        this.strategy = strategy;
        this.archiveLocation = archiveLocation;
        this.errorLocation = errorLocation;
        this.adapterClass = adapterClass;
        this.adapterUri = adapterUri;
        this.delay = delay;
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
    public String getAdapterUri() {
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

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public void setArchiveLocation(String archiveLocation) {
        this.archiveLocation = archiveLocation;
    }

    public void setErrorLocation(String errorLocation) {
        this.errorLocation = errorLocation;
    }

    public void setAdapterClass(String adapterClass) {
        this.adapterClass = adapterClass;
    }

    public void setAdapterUri(String adapterUri) {
        this.adapterUri = adapterUri;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}
