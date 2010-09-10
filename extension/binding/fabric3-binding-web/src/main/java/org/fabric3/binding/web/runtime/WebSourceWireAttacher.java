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
package org.fabric3.binding.web.runtime;

import javax.servlet.ServletException;

import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.handler.ReflectorServletProcessor;
import org.oasisopen.sca.annotation.Reference;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.web.provision.WebSourceDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.host.ServletHost;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class WebSourceWireAttacher implements SourceWireAttacher<WebSourceDefinition> {
    private static final String CONTEXT_PATH = "web";
    private PubSubManager pubSubManager;
    private ServletHost servletHost;
    private GatewayServlet gatewayServlet;

    public WebSourceWireAttacher(@Reference PubSubManager pubSubManager, @Reference ServletHost servletHost) {
        this.pubSubManager = pubSubManager;
        this.servletHost = servletHost;
    }

    public void attach(WebSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        GatewayServletContext context = new GatewayServletContext(CONTEXT_PATH);
        // TODO support other configuration as specified in AtmosphereServlet init()
        context.setInitParameter(AtmosphereServlet.PROPERTY_SESSION_SUPPORT, "false");
        context.setInitParameter(AtmosphereServlet.WEBSOCKET_ATMOSPHEREHANDLER, "false");   // turn the handler off as it is overriden below
        context.setInitParameter(AtmosphereServlet.WEBSOCKET_SUPPORT, "true");

        GatewayServletConfig config = new GatewayServletConfig(context);
        gatewayServlet = new GatewayServlet(servletHost, pubSubManager);

        try {
            gatewayServlet.init(config);

            ReflectorServletProcessor processor = new ReflectorServletProcessor();
            //processor.setServlet(router);
            processor.init(config);
            //WebSocketHandler webSocketHandler = new WebSocketHandler(processor, broadcasterManager);
            //gatewayServlet.addAtmosphereHandler("/*", webSocketHandler);
            servletHost.registerMapping(CONTEXT_PATH, gatewayServlet);
        } catch (ServletException e) {
            throw new WiringException(e);
        }

    }

    public void detach(WebSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {

    }

    public void attachObjectFactory(WebSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalTargetDefinition target) {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(WebSourceDefinition source, PhysicalTargetDefinition target) {
        throw new UnsupportedOperationException();
    }
}
