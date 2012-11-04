/*
 * Fabric3 Copyright (c) 2009-2012 Metaform Systems
 * 
 * Fabric3 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version, with the following exception:
 * 
 * Linking this software statically or dynamically with other modules is making
 * a combined work based on this software. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this software give you
 * permission to link this software with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this software. If you modify
 * this software, you may extend this exception to your version of the software,
 * but you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 * 
 * Fabric3 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Fabric3. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fabric3.binding.file.provision;

import java.net.URI;

import org.fabric3.binding.file.common.Strategy;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;

/**
 * Generated metadata used for attaching a service to a file listener.
 */
public class FileBindingSourceDefinition extends PhysicalSourceDefinition {
    private static final long serialVersionUID = -4154935681094596517L;

    private String location;
    private Strategy strategy;
    private String archiveLocation;
    private String errorLocation;
    private String adapterClass;
    private URI adapterUri;
    private String pattern;
    private long delay;

    public FileBindingSourceDefinition(URI uri,
                                       String pattern, String location,
                                       Strategy strategy,
                                       String archiveLocation,
                                       String errorLocation,
                                       String adapterClass,
                                       URI adapterUri,
                                       long delay) {
        this.pattern = pattern;
        this.location = location;
        this.strategy = strategy;
        this.archiveLocation = archiveLocation;
        this.errorLocation = errorLocation;
        this.adapterClass = adapterClass;
        this.adapterUri = adapterUri;
        this.delay = delay;
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
     * The directory to place invalid files in. May be relative or absolute. If it is relative, it will be resolved against the runtime data
     * directory
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
}
