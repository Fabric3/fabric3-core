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
package org.fabric3.implementation.web.model;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.component.Implementation;

/**
 * Model object for a web component.
 */
public class WebImplementation extends Implementation<WebComponentType> {
    private static final long serialVersionUID = 5589199308230767243L;
    // the deprecated, F3-specific namespace
    @Deprecated
    public static final QName IMPLEMENTATION_WEBAPP = new QName(Namespaces.F3, "web");
    public static final QName IMPLEMENTATION_WEB = new QName(org.oasisopen.sca.Constants.SCA_NS, "implementation.web");

    private URI uri;

    /**
     * Default constructor. Used to create a web component implementation whose web app context URL will be constructed using the component URI.
     */
    public WebImplementation() {
    }

    /**
     * Constructor. Used to create a web component implementation whose web app context URL will be constructed using the component URI.
     *
     * @param uri the URI used when creating the web app context URL.
     */
    public WebImplementation(URI uri) {
        this.uri = uri;
    }

    public QName getType() {
        return IMPLEMENTATION_WEB;
    }

    public URI getUri() {
        return uri;
    }

}
