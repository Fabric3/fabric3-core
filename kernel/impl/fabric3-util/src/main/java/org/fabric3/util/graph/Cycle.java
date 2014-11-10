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
package org.fabric3.util.graph;

import java.util.List;

/**
 * Represents a cycle in a directed graph
 */
public class Cycle<T> {
    private List<Vertex<T>> originPath;
    private List<Vertex<T>> backPath;

    /**
     * Returns the list of vertices from the cycle origin to the endpoint.
     *
     * @return the list of vertices from the cycle origin to the endpoint
     */
    public List<Vertex<T>> getOriginPath() {
        return originPath;
    }

    /**
     * Sets the list of vertices from the cycle origin to the endpoint.
     *
     * @param originPath the list of vertices from the cycle origin to the endpoint
     */
    public void setOriginPath(List<Vertex<T>> originPath) {
        this.originPath = originPath;
    }

    /**
     * Returns the list of vertices from the cycle endpoint back to the origin.
     *
     * @return the the list of vertices from the cycle endpoint back to the origin
     */
    public List<Vertex<T>> getBackPath() {
        return backPath;
    }

    /**
     * Sets the list of vertices from the cycle endpoint back to the origin.
     *
     * @param backPath the the list of vertices from the cycle endpoint back to the origin
     */
    public void setBackPath(List<Vertex<T>> backPath) {
        this.backPath = backPath;
    }
}
