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
package org.fabric3.binding.ws.metro.runtime.core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import com.sun.xml.wss.SecurityEnvironment;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Creates JAX-WS <code>Dispatch</code> instances that can be shared among wire invocation chains.
 *
 * @version $Rev$ $Date$
 */
public class MetroDispatchObjectFactory implements ObjectFactory<Dispatch<Source>> {
    private QName serviceName;
    private QName portName;
    private WebServiceFeature[] features;
    private File wsitConfiguration;
    private ExecutorService executorService;
    private SecurityEnvironment securityEnvironment;
    private Dispatch<Source> dispatch;
    private URL wsdlLocation;

    /**
     * Constructor.
     *
     * @param endpointDefinition  the target endpoint definition
     * @param wsdlLocation        the WSDL defining the target service contract
     * @param wsitConfiguration   WSIT policy configuration for the proxy, or null if policy is not configured
     * @param features            web services features to enable on the generated proxy
     * @param executorService     the executor service used for dispatching invocations
     * @param securityEnvironment the Metro host runtime security SPI implementation
     */
    public MetroDispatchObjectFactory(ReferenceEndpointDefinition endpointDefinition,
                                      URL wsdlLocation,
                                      File wsitConfiguration,
                                      WebServiceFeature[] features,
                                      ExecutorService executorService,
                                      SecurityEnvironment securityEnvironment) {
        this.wsdlLocation = wsdlLocation;
        this.serviceName = endpointDefinition.getServiceName();
        this.portName = endpointDefinition.getPortName();
        this.features = features;
        this.wsitConfiguration = wsitConfiguration;
        this.executorService = executorService;
        this.securityEnvironment = securityEnvironment;
    }

    public Dispatch<Source> getInstance() throws ObjectCreationException {
        if (dispatch == null) {
            // there is a possibility more than one proxy will be created but since this does not have side-effects, avoid synchronization
            dispatch = createProxy();
        }
        return dispatch;
    }

    /**
     * Lazily creates the service proxy. Proxy creation is done during the first invocation as the target service may not be available when the client
     * that the proxy is to be injected into is instantiated. The proxy is later cached for subsequent invocations.
     *
     * @return the web service proxy
     * @throws ObjectCreationException if there was an error creating the proxy
     */
    private Dispatch<Source> createProxy() throws ObjectCreationException {
        try {
            Service service;
            WSService.InitParams params = new WSService.InitParams();
            WsitClientConfigurationContainer container;
            if (wsitConfiguration != null) {
                // Policy configured
                // FIXME
                container = new WsitClientConfigurationContainer(wsitConfiguration.toURI().toURL(), securityEnvironment);
            } else {
                // No policy
                container = new WsitClientConfigurationContainer(securityEnvironment);
            }
            params.setContainer(container);
            service = WSService.create(wsdlLocation, serviceName, params);
            // use the kernel scheduler for dispatching
            service.setExecutor(executorService);
            return service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD, features);
        } catch (InaccessibleWSDLException e) {
            throw new ObjectCreationException(e);
        } catch (MalformedURLException e) {
            throw new ObjectCreationException(e);
        }


    }


}