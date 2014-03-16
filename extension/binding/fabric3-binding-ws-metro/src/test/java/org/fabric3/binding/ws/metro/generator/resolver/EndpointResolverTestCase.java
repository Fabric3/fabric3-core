package org.fabric3.binding.ws.metro.generator.resolver;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.binding.ws.metro.generator.WsdlElement;
import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.wsdl.contribution.impl.Wsdl4JFactoryImpl;

public class EndpointResolverTestCase extends TestCase {
	
	public void testWsdlSOAP12() throws Exception {
		Wsdl4JFactoryImpl wsdlFactory = new Wsdl4JFactoryImpl();
		Definition wsdl = wsdlFactory.newReader().readWSDL("src/test/resources/AddNumbersSOAP12.wsdl");
		EndpointResolverImpl resolver = new EndpointResolverImpl(wsdlFactory);
		
		Service service = (Service)wsdl.getServices().values().iterator().next();
		QName serviceQ = service.getQName();		
		String port = ((Port)service.getPorts().values().iterator().next()).getName();
		
		QName portQ = new QName(serviceQ.getNamespaceURI(), port);		
		WsdlElement wsdlElement = new WsdlElement(serviceQ, portQ);
		
		ReferenceEndpointDefinition def = resolver.resolveReferenceEndpoint(wsdlElement, wsdl);
		assertNotNull(def.getUrl());
		
		ServiceEndpointDefinition def1 = resolver.resolveServiceEndpoint(wsdlElement, wsdl);
		assertNotNull(def1.getServicePath());	
    }
	
	public void testWsdlSOAP11() throws Exception {
		Wsdl4JFactoryImpl wsdlFactory = new Wsdl4JFactoryImpl();
		Definition wsdl = wsdlFactory.newReader().readWSDL("src/test/resources/WeatherClientSide.wsdl");
		EndpointResolverImpl resolver = new EndpointResolverImpl(wsdlFactory);
		
		Service service = (Service)wsdl.getServices().values().iterator().next();
		QName serviceQ = service.getQName();		
		String port = ((Port)service.getPorts().values().iterator().next()).getName();
		
		QName portQ = new QName(serviceQ.getNamespaceURI(), port);		
		WsdlElement wsdlElement = new WsdlElement(serviceQ, portQ);
				
		ServiceEndpointDefinition def1 = resolver.resolveServiceEndpoint(wsdlElement, wsdl);
		assertNotNull(def1.getServicePath());	
		
		ReferenceEndpointDefinition def = resolver.resolveReferenceEndpoint(wsdlElement, wsdl);
		assertNotNull(def.getUrl());
    }

}
