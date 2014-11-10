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

import java.net.URL;
import javax.xml.namespace.QName;

import org.fabric3.api.model.type.ModelObject;

/**
 * A composite include.
 */
public class Include extends ModelObject<Composite> {
    private static final long serialVersionUID = 3982129607792011105L;

    private QName name;
    private URL scdlLocation;
    private Composite included;

    /**
     * Returns the name of the composite that is being included.
     *
     * @return the name of the composite that is being included
     */
    public QName getName() {
        return name;
    }

    /**
     * Sets the name of the composite that is being included.
     *
     * @param name the name of the composite that is being included
     */
    public void setName(QName name) {
        this.name = name;
    }

    /**
     * Returns the location of the SCDL for composite being included.
     *
     * @return the location of the SCDL for composite being included
     */
    public URL getScdlLocation() {
        return scdlLocation;
    }

    /**
     * Sets the location of the SCDL for composite being included.
     *
     * @param scdlLocation the location of the SCDL for composite being included
     */
    public void setScdlLocation(URL scdlLocation) {
        this.scdlLocation = scdlLocation;
    }

    /**
     * Returns the composite that was included.
     *
     * @return the composite that was included
     */
    public Composite getIncluded() {
        return included;
    }

    /**
     * Sets the composite that was included.
     *
     * @param included the composite that was included
     */
    public void setIncluded(Composite included) {
        this.included = included;
    }

}
