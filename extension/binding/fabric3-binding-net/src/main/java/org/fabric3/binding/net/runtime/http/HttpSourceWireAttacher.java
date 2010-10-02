/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.binding.net.runtime.http;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.net.provision.HttpSourceDefinition;
import org.fabric3.binding.net.runtime.TransportService;
import org.fabric3.binding.net.runtime.WireHolder;
import org.fabric3.spi.objectfactory.ObjectFactory;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.binding.format.ParameterEncoderFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches services to an HTTP channel.
 *
 * @version $Rev$ $Date$
 */
public class HttpSourceWireAttacher implements SourceWireAttacher<HttpSourceDefinition> {
    private TransportService service;
    private ClassLoaderRegistry classLoaderRegistry;
    private Map<String, ParameterEncoderFactory> formatterFactories = new HashMap<String, ParameterEncoderFactory>();

    public HttpSourceWireAttacher(@Reference TransportService service, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.service = service;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Reference
    public void setFormatterFactories(Map<String, ParameterEncoderFactory> formatterFactories) {
        this.formatterFactories = formatterFactories;
    }

    public void attach(HttpSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        URI uri = source.getUri();
        if (uri.getScheme() != null) {
            throw new WiringException("Absolute URIs not supported: " + uri);
        }
        String sourceUri = uri.toString();
        String callbackUri = null;
        if (target.getCallbackUri() != null) {
            callbackUri = target.getCallbackUri().toString();
        }
        String wireFormat = source.getConfig().getWireFormat();
        if (wireFormat == null) {
            wireFormat = "jaxb";
        }
        ParameterEncoderFactory formatterFactory = formatterFactories.get(wireFormat);
        if (formatterFactory == null) {
            throw new WiringException("WireFormatterFactory not found for: " + wireFormat);
        }
        URI id = source.getClassLoaderId();
        ClassLoader loader = classLoaderRegistry.getClassLoader(id);
        WireHolder wireHolder = createWireHolder(wire, callbackUri, formatterFactory, loader);
        service.registerHttp(sourceUri, wireHolder);
    }

    public void detach(HttpSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        service.unregisterHttp(source.getUri().toString());
    }

    public void attachObjectFactory(HttpSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target)
            throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(HttpSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private WireHolder createWireHolder(Wire wire, String callbackUri, ParameterEncoderFactory formatterFactory, ClassLoader loader)
            throws WiringException {
        try {
            List<InvocationChain> chains = wire.getInvocationChains();
            ParameterEncoder formatter = formatterFactory.getInstance(wire, loader);
            return new WireHolder(chains, formatter, callbackUri);
        } catch (EncoderException e) {
            throw new WiringException(e);
        }
    }

}
