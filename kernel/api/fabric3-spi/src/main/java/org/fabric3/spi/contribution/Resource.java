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
package org.fabric3.spi.contribution;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.host.stream.Source;

/**
 * Represents a resource in a contribution such as a WSDL document or composite.
 */
public class Resource {
    private List<ResourceElement<?, ?>> elements = new ArrayList<ResourceElement<?, ?>>();
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
