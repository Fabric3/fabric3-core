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
package org.fabric3.binding.ws.metro.provision;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.fabric3.spi.model.physical.PhysicalBindingHandlerDefinition;

/**
 * Wire source definition for services that use Java interface-based contracts.
 */
public class MetroJavaWireSourceDefinition extends MetroWireSourceDefinition {
    private static final long serialVersionUID = 2898989563911925959L;

    private String interfaze;
    private byte[] generatedInterface;
    private URI classLoaderUri;
    private Map<String, String> schemas;

    private URL wsdlLocation;

    /**
     * Constructor.
     *
     * @param serviceUri         the structural service URI
     * @param endpointDefinition endpoint metadata
     * @param interfaze          the service contract (SEI) name.
     * @param generatedInterface the generated SEI bytes or null if no interface needed to be generated
     * @param classLoaderUri     the classloader for SEI types
     * @param wsdl               the generated WSDL containing merged policy or null if no policy applies to the endpoint
     * @param schemas            the schemas imported by the generated WSDL or null
     * @param providedIntents    intents configured at the endpoint level that are provided natively by the Metro
     * @param wsdlLocation       optional URL to the WSDL location
     * @param bidirectional      true if the wire this definition is associated with is bidirectional, i.e. has a callback
     * @param handlers           optional binding handlers
     */
    public MetroJavaWireSourceDefinition(URI serviceUri,
                                         ServiceEndpointDefinition endpointDefinition,
                                         String interfaze,
                                         byte[] generatedInterface,
                                         URI classLoaderUri,
                                         String wsdl,
                                         Map<String, String> schemas,
                                         List<QName> providedIntents,
                                         URL wsdlLocation,
                                         boolean bidirectional,
                                         List<PhysicalBindingHandlerDefinition> handlers) {
        super(serviceUri, endpointDefinition, wsdl, providedIntents, bidirectional, handlers);
        this.interfaze = interfaze;
        this.generatedInterface = generatedInterface;
        this.classLoaderUri = classLoaderUri;
        this.schemas = schemas;
        this.wsdlLocation = wsdlLocation;
    }

    /**
     * Returns the service contract name.
     *
     * @return the service contract name
     */
    public String getInterface() {
        return interfaze;
    }

    /**
     * Returns the generated SEI interface bytes.
     *
     * @return the generated SEI interface bytes
     */
    public byte[] getGeneratedInterface() {
        return generatedInterface;
    }

    /**
     * Returns any associated WSDLs with the schemas.
     *
     * @return any associated WSDLs with the schemas
     */
    public Map<String, String> getSchemas() {
        return schemas;
    }

    /**
     * Returns an optional URL to the WSDL document.
     *
     * @return optional URL to the WSDL document
     */
    public URL getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * Returns the SEI classloader URI.
     *
     * @return the SEI classloader URI
     */
    public URI getSEIClassLoaderUri() {
        return classLoaderUri;
    }
}