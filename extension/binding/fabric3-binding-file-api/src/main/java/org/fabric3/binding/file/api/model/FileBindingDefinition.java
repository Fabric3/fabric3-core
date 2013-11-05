/*
 * Fabric3
 * Copyright (c) 2009-2013 Metaform Systems
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
 */
package org.fabric3.binding.file.api.model;

import javax.xml.namespace.QName;

import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.binding.file.api.annotation.Strategy;

/**
 * A file binding configuration set on a reference.
 */
public class FileBindingDefinition extends BindingDefinition {
    private static final long serialVersionUID = -8904535030035183877L;
    public static final QName BINDING_FILE = new QName(org.fabric3.api.Namespaces.F3, "binding.file");

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
