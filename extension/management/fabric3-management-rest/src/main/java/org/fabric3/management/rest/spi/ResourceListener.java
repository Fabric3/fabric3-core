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
package org.fabric3.management.rest.spi;

/**
 * Receives callbacks when resources are exported.
 */
public interface ResourceListener {

    /**
     * Callback received when a root resource has been exported
     *
     * @param mapping the root resource mapping
     */
    void onRootResourceExport(ResourceMapping mapping);

    /**
     * Callback received when a sub-resource has been exported
     *
     * @param mapping the sub-resource mapping
     */
    void onSubResourceExport(ResourceMapping mapping);

    /**
     * Callback received when a root resource has been removed
     *
     * @param identifier the resource registration identifier
     */
    void onRootResourceRemove(String identifier);

    /**
     * Callback received when a sub-resource has been removed
     *
     * @param identifier the resource registration identifier
     */
    void onSubResourceRemove(String identifier);

}
