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
package com.sun.xml.ws.developer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Map;

import com.sun.istack.NotNull;
import com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import org.w3c.dom.Element;

/**
 * This class overrides the one contained in Metro to add missing namespace declarations that results in the following error on WebLogic when invoking a
 * reference configured with binding.ws:
 * <pre>
 * Error creating JAXBContext for W3CEndpointReference.
 * 	at com.sun.xml.ws.spi.ProviderImpl$2.run(ProviderImpl.java:261)
 * 	at com.sun.xml.ws.spi.ProviderImpl$2.run(ProviderImpl.java:257)
 * 	at java.security.AccessController.doPrivileged(Native Method)
 * 	at com.sun.xml.ws.spi.ProviderImpl.getEPRJaxbContext(ProviderImpl.java:256)
 * 	at com.sun.xml.ws.spi.ProviderImpl.<clinit>(ProviderImpl.java:90)
 * 	....
 * Caused By: com.sun.xml.bind.v2.runtime.IllegalAnnotationsException: 2 counts of IllegalAnnotationExceptions
 * Two classes have the same XML type name "address". Use @XmlType.name and @XmlType.namespace to assign different names to them.
 * 	this problem is related to the following location:
 * 		at com.sun.xml.ws.developer.MemberSubmissionEndpointReference$Address
 * 		at public com.sun.xml.ws.developer.MemberSubmissionEndpointReference$Address com.sun.xml.ws.developer.MemberSubmissionEndpointReference.addr
 * 		at com.sun.xml.ws.developer.MemberSubmissionEndpointReference
 * 	this problem is related to the following location:
 * 		at javax.xml.ws.wsaddressing.W3CEndpointReference$Address
 * 		at private javax.xml.ws.wsaddressing.W3CEndpointReference$Address javax.xml.ws.wsaddressing.W3CEndpointReference.address
 * 		at javax.xml.ws.wsaddressing.W3CEndpointReference
 * 	</pre>
 * Note this problem is a general one (and not specific to Fabric3) running Metro 2.0 as a client in WLS.
 *
 * This class is guaranteed to override the one contained in the Metro jar as extension classes are placed on the contribution classpath prior to those
 * contained in META-INF/lib jars.
 */
@XmlRootElement(name = "EndpointReference", namespace = "http://schemas.xmlsoap.org/ws/2004/08/addressing")
@XmlType(name = "EndpointReferenceType", namespace = "http://schemas.xmlsoap.org/ws/2004/08/addressing")
public final class MemberSubmissionEndpointReference extends EndpointReference implements MemberSubmissionAddressingConstants {
    private static final JAXBContext msjc = getMSJaxbContext();

    @XmlElement(name = "Address", namespace = "http://schemas.xmlsoap.org/ws/2004/08/addressing")
    public Address addr;

    @XmlElement(name = "ReferenceProperties", namespace = "http://schemas.xmlsoap.org/ws/2004/08/addressing")
    public Elements referenceProperties;

    @XmlElement(name = "ReferenceParameters", namespace = "http://schemas.xmlsoap.org/ws/2004/08/addressing")
    public Elements referenceParameters;

    @XmlElement(name = "PortType", namespace = "http://schemas.xmlsoap.org/ws/2004/08/addressing")
    public AttributedQName portTypeName;

    @XmlElement(name = "ServiceName", namespace = "http://schemas.xmlsoap.org/ws/2004/08/addressing")
    public ServiceNameType serviceName;

    @XmlAnyAttribute
    public Map<QName, String> attributes;

    @XmlAnyElement
    public List<Element> elements;
    protected static final String MSNS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";

    public MemberSubmissionEndpointReference() {
    }

    public MemberSubmissionEndpointReference(@NotNull Source source) {
        if (source == null) {
            throw new WebServiceException("Source parameter can not be null on constructor");
        }
        try {
            Unmarshaller unmarshaller = msjc.createUnmarshaller();
            MemberSubmissionEndpointReference epr = unmarshaller.unmarshal(source, MemberSubmissionEndpointReference.class).getValue();

            this.addr = epr.addr;
            this.referenceProperties = epr.referenceProperties;
            this.referenceParameters = epr.referenceParameters;
            this.portTypeName = epr.portTypeName;
            this.serviceName = epr.serviceName;
            this.attributes = epr.attributes;
            this.elements = epr.elements;
        } catch (JAXBException e) {
            throw new WebServiceException("Error unmarshalling MemberSubmissionEndpointReference ", e);
        } catch (ClassCastException e) {
            throw new WebServiceException("Source did not contain MemberSubmissionEndpointReference", e);
        }
    }

    public void writeTo(Result result) {
        try {
            Marshaller marshaller = msjc.createMarshaller();

            marshaller.marshal(this, result);
        } catch (JAXBException e) {
            throw new WebServiceException("Error marshalling W3CEndpointReference. ", e);
        }
    }

    public Source toWSDLSource() {
        Element wsdlElement = null;

        for (Element elem : this.elements) {
            if ((elem.getNamespaceURI().equals("http://schemas.xmlsoap.org/wsdl/"))
                && (elem.getLocalName().equals(WSDLConstants.QNAME_DEFINITIONS.getLocalPart()))) {
                wsdlElement = elem;
            }
        }

        return new DOMSource(wsdlElement);
    }

    private static JAXBContext getMSJaxbContext() {
        try {
            return JAXBContext.newInstance(MemberSubmissionEndpointReference.class);
        } catch (JAXBException e) {
            throw new WebServiceException("Error creating JAXBContext for MemberSubmissionEndpointReference. ", e);
        }
    }

    public static class ServiceNameType extends MemberSubmissionEndpointReference.AttributedQName {

        @XmlAttribute(name = "PortName")
        public String portName;
    }

    public static class AttributedQName {

        @XmlValue
        public QName name;

        @XmlAnyAttribute
        public Map<QName, String> attributes;
    }

    // Added XmlType declaration to specify namespace
    @XmlType(name = "elements", namespace = MemberSubmissionEndpointReference.MSNS)
    public static class Elements {

        @XmlAnyElement
        public List<Element> elements;
    }

    // Added XmlType declaration to specify namespace
    @XmlType(name = "address", namespace = MemberSubmissionEndpointReference.MSNS)
    public static class Address {

        @XmlValue
        public String uri;

        @XmlAnyAttribute
        public Map<QName, String> attributes;
    }
}