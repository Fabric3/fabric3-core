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
