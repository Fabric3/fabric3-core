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
package org.fabric3.spi.xml;

import javax.xml.stream.Location;

/**
 * Used to override a Location system id.
 */
public class LocationWrapper implements Location {
    private String systemId;
    private Location delegate;

    public LocationWrapper(Location delegate, String systemId) {
        this.delegate = delegate;
        this.systemId = systemId;
    }

    public int getLineNumber() {
        return delegate.getLineNumber();
    }

    public int getColumnNumber() {
        return delegate.getColumnNumber();
    }

    public int getCharacterOffset() {
        return delegate.getCharacterOffset();
    }

    public String getPublicId() {
        return delegate.getPublicId();
    }

    public String getSystemId() {
        return systemId;
    }
}
