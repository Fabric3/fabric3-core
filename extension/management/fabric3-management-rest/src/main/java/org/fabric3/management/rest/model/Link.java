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
package org.fabric3.management.rest.model;

import java.net.URL;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A link to a resource.
 */
public class Link {

    public static final String SELF_LINK = "self";
    public static final String ALTERNATE_LINK = "alternate";
    public static final String EDIT_LINK = "edit";
    public static final String RELATED_LINK = "related";
    public static final String PREVIOUS_LINK = "previous";
    public static final String NEXT_LINK = "next";
    public static final String FIRST_LINK = "first";
    public static final String LAST_LINK = "last";

    @JsonProperty
    private String name;
    @JsonProperty
    private String rel;
    @JsonProperty
    private URL href;

    /**
     * Constructor for databinding.
     */
    protected Link() {
    }

    /**
     * Constructor.
     *
     * @param name the link name
     * @param rel  the relationship the linked resource has to the enclosing entity
     * @param href the linked resource URL
     */
    public Link(String name, String rel, URL href) {
        this.name = name;
        this.rel = rel;
        this.href = href;
    }

    /**
     * Returns the link name.
     *
     * @return the link name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the link relationship.
     *
     * @return the link relationship
     */
    public String getRel() {
        return rel;
    }

    /**
     * Returns the linked resource URL.
     *
     * @return the linked resource URL
     */
    public URL getHref() {
        return href;
    }
}
