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
package org.fabric3.runtime.maven.repository;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Description of some packaged artifact such as a JAR file or a Composite.
 *
 * @version $Rev$ $Date$
 */
public class Artifact {

    /* Artifact group */
    private String group;

    /* Artifact name */
    private String name;

    /* Artifact version */
    private String version;

    /* Artifact classifier */
    private String classifier;

    /* Artifact type */
    private String type;

    /* Artifact url */
    private URL url;

    /* Transitive dependencies */
    private Set<Artifact> dependencies = new HashSet<Artifact>();

    /**
     * Adds a transitive dependency to the artifact.
     *
     * @param artifact Dependency to be added.
     */
    public void addDependency(Artifact artifact) {
        dependencies.add(artifact);
    }

    /**
     * Gets the URLs for all the transitive dependencies.
     *
     * @return Sets of URLs for all the transitive dependencies.
     */
    public Set<URL> getUrls() {

        Set<URL> urls = new HashSet<URL>();

        for (Artifact artifact : dependencies) {
            urls.add(artifact.getUrl());
        }
        urls.add(getUrl());

        return urls;

    }

    /**
     * Returns the name of a logical grouping to which this artifact belongs. For example, this might represent the original publisher of the
     * artifact.
     *
     * @return the name of a logical grouping to which this artifact belongs
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the name of a logical grouping to which this artifact belongs.
     *
     * @param group the name of a logical grouping to which this artifact belongs
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Returns the name of an artifact.
     *
     * @return the name of an artifact
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of an artifact.
     *
     * @param name the name of an artifact
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the version of an artifact.
     *
     * @return the version of an artifact
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of an artifact.
     *
     * @param version the version of an artifact
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns a way of classifying an artifact. This can be used to distinguish variants of an artifact that provide the same function but which may
     * have platform specific requirements. For example, it may contain the name of a hardware platform for artifacts that contain native code.
     *
     * @return a way of classifying an artifact
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Sets a way of classifying an artifact
     *
     * @param classifier a way of classifying an artifact
     */
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    /**
     * Returns the type of artifact.
     *
     * @return the type of artifact
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of artifact.
     *
     * @param type the type of artifact
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns a URL from which the artifact can be obtained.
     *
     * @return a URL from which the artifact can be obtained
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets a URL from which the artifact can be obtained.
     *
     * @param url a URL from which the artifact can be obtained
     */
    public void setUrl(URL url) {
        this.url = url;
    }


    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(group).append(':').append(name).append(':').append(version).append(':').append(type);
        return buf.toString();
    }
}
