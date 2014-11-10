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
package org.fabric3.spi.model.os;

import java.io.Serializable;
import java.util.List;

/**
 * Identifies a native library in a contribution manifest.
 */
public class Library implements Serializable {
    private static final long serialVersionUID = -3440164417832624801L;

    private String path;
    private List<OperatingSystemSpec> operatingSystems;

    public Library(String path, List<OperatingSystemSpec> operatingSystems) {
        this.path = path;
        this.operatingSystems = operatingSystems;
    }

    public String getPath() {
        return path;
    }

    public List<OperatingSystemSpec> getOperatingSystems() {
        return operatingSystems;
    }
}
