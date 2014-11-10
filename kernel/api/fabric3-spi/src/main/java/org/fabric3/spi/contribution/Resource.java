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
package org.fabric3.spi.contribution;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.stream.Source;

/**
 * Represents a resource in a contribution such as a WSDL document or composite.
 */
public class Resource {
    private List<ResourceElement<?, ?>> elements = new ArrayList<>();
    private Source source;
    private String contentType;
    private Contribution contribution;

    private ResourceState state = ResourceState.UNPROCESSED;

    public Resource(Contribution contribution, Source source, String contentType) {
        this.contribution = contribution;
        this.source = source;
        this.contentType = contentType;
    }

    /**
     * Returns the contribution this resource is contained in.
     *
     * @return the contribution this resource is contained in
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Overrides the contribution this resource is contained in.
     *
     * @param contribution the containing contribution
     */
    public void setContribution(Contribution contribution) {
        this.contribution = contribution;
    }

    /**
     * Returns the resource content type
     *
     * @return the resource content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the source for reading the resource contents.
     *
     * @return the source
     */
    public Source getSource() {
        return source;
    }

    /**
     * Adds a resource element.
     *
     * @param element the resourceElement
     */
    public void addResourceElement(ResourceElement<?, ?> element) {
        elements.add(element);
        element.setResource(this);
    }

    /**
     * Returns a map of resource elements keyed by their symbol.
     *
     * @return the map of resource elements
     */
    public List<ResourceElement<?, ?>> getResourceElements() {
        return elements;
    }

    /**
     * Returns resource state.
     *
     * @return the resource state
     */
    public ResourceState getState() {
        return state;
    }

    /**
     * Sets the resource state.
     *
     * @param state the resource state
     */
    public void setState(ResourceState state) {
        this.state = state;
    }
}
