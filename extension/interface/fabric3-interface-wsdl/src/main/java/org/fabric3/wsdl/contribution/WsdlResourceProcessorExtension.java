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
package org.fabric3.wsdl.contribution;

import javax.wsdl.Definition;

import org.fabric3.spi.contribution.Resource;

/**
 * Implementations extend resource processing during contribution installation to handle WSDL extensibility elements.
 */
public interface WsdlResourceProcessorExtension {

    /**
     * Callback to receive notification when a WSDL definition is being processed duriny contribution installation.
     *
     * @param resource   the resource metadata
     * @param definition the parsed WSDL
     */
    void process(Resource resource, Definition definition);
}
