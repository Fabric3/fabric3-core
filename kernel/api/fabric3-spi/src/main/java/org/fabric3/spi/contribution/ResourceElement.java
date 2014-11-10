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

/**
 * An addressable part of a Resource, such as a WSDL PortType, ComponentType, or Schema.
 */
public class ResourceElement<SYMBOL extends Symbol, VALUE> {
    private SYMBOL symbol;
    private VALUE value;
    private Resource resource;
    private Object metadata;

    public ResourceElement(SYMBOL symbol) {
        this.symbol = symbol;
    }

    public ResourceElement(SYMBOL symbol, VALUE value) {
        this.symbol = symbol;
        this.value = value;
    }

    /**
     * Returns the resource this element is contained in.
     *
     * @return the resource this element is contained in.
     */
    public Resource getResource() {
        return resource;
    }

    /**
     * Sets the resource this element is contained in.
     *
     * @param resource the resource this element is contained in.
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Returns the symbol the resource element is indexed by.
     *
     * @return the symbol the resource element is indexed by.
     */
    public SYMBOL getSymbol() {
        return symbol;
    }

    /**
     * Returns the actual resource element.
     *
     * @return the resource element
     */
    public VALUE getValue() {
        return value;
    }

    /**
     * Sets the actual resource element.
     *
     * @param value the resource element
     */
    public void setValue(VALUE value) {
        this.value = value;
    }

    /**
     * Returns metadata associated with the resource element or null.
     *
     * @param type the metadata type
     * @return the metadata or null
     */
    public <T> T getMetadata(Class<T> type) {
        return type.cast(metadata);
    }

    /**
     * Sets metadata for the resource element.
     *
     * @param metadata the metadata
     */
    public void setMetadata(Object metadata) {
        this.metadata = metadata;
    }
}
