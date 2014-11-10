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
package org.fabric3.api.model.type.component;

/**
 * Denotes if autowire is on, off, or inherited.
 */
public enum Autowire {
    ON,
    OFF,
    INHERITED;

    /**
     * Parse an autowire value.
     *
     * @param text the text to parse
     * @return INHERITED if the text is null or empty, ON if text is "true", otherwise OFF
     */
    public static Autowire fromString(String text) {
        if (text == null || text.length() == 0) {
            return INHERITED;
        } else if ("true".equalsIgnoreCase(text)) {
            return ON;
        } else {
            return OFF;
        }
    }
}
