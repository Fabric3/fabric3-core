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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.api.binding.jms.model;

/**
 * Represents binding.jms\operationProperties.
 */
public class OperationPropertiesDefinition extends PropertyAwareObject {
    private static final long serialVersionUID = -1325680761205311178L;
    private String name;
    private String selectedOperation;
    private HeadersDefinition header = new HeadersDefinition();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelectedOperation() {
        return selectedOperation;
    }

    public void setSelectedOperation(String operation) {
        this.selectedOperation = operation;
    }

    public HeadersDefinition getHeaders() {
        return header;
    }

}
