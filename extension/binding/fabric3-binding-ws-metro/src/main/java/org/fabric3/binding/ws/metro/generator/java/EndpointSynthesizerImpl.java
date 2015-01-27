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
package org.fabric3.binding.ws.metro.generator.java;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URL;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.spi.model.type.java.JavaServiceContract;

/**
 * Default EndpointSynthesizer implementation.
 */
public class EndpointSynthesizerImpl implements EndpointSynthesizer {

    public ReferenceEndpointDefinition synthesizeReferenceEndpoint(JavaServiceContract contract, Class<?> serviceClass, URL url) {
        WebService annotation = serviceClass.getAnnotation(WebService.class);
        if (annotation != null) {
            return createDefinition(annotation, serviceClass, url, null);
        } else {
            return createDefaultDefinition(serviceClass, url, null);
        }
    }

    public ServiceEndpointDefinition synthesizeServiceEndpoint(JavaServiceContract contract, Class<?> serviceClass, URI uri) {
        WebService annotation = serviceClass.getAnnotation(WebService.class);
        if (annotation != null) {
            return createDefinition(annotation, serviceClass, uri);
        } else {
            return createDefaultDefinition(serviceClass, uri);
        }
    }

    /**
     * Creates a ReferenceEndpointDefinition following JAX-WS rules (section 3.11) for deriving service and port names from a class containing an
     * <code>@WebService</code> annotation.
     *
     * @param annotation   the annotation
     * @param serviceClass the service class
     * @param url          the target endpoint URL
     * @param portTypeQName the port type name or null if one should be generated
     * @return the ServiceEndpointDefinition
     */
    private ReferenceEndpointDefinition createDefinition(WebService annotation, Class<?> serviceClass, URL url, QName portTypeQName) {
        String namespace = getNamespace(annotation, serviceClass);
        ServiceNameResult result = getServiceName(annotation, serviceClass, namespace);
        QName serviceQName = result.getServiceName();
        boolean defaultService = result.isDefaultServiceName();
        QName portQName = null;   // don't generate a port name unless a port type name is also generated BWS_2012
        if (portTypeQName == null) {
            portTypeQName = getPortTypeName(annotation, serviceClass, namespace);
            portQName = getPortName(annotation, serviceClass, namespace);
        }
        return new ReferenceEndpointDefinition(serviceQName, defaultService, portQName, portTypeQName, url);
    }

    /**
     * Creates a ReferenceEndpointDefinition following JAX-WS rules (section 3.11) for deriving default service and port names from a class.
     *
     * @param serviceClass the service class
     * @param url          the target endpoint URL
     * @param portTypeQName the port type name or null if one should be generated
     * @return the ServiceEndpointDefinition
     */
    private ReferenceEndpointDefinition createDefaultDefinition(Class<?> serviceClass, URL url, QName portTypeQName) {
        String packageName = serviceClass.getPackage().getName();
        String className = serviceClass.getSimpleName();
        String namespace = deriveNamespace(packageName);
        QName serviceQName = new QName(namespace, className + "Service");
        QName portQName = null;   // don't generate a port name unless a port type name is also generated BWS_2012
        if (portTypeQName == null) {
            portTypeQName = new QName(namespace, className);
            portQName = new QName(namespace, className + "Port");
        }
        return new ReferenceEndpointDefinition(serviceQName, true, portQName, portTypeQName, url);
    }

    /**
     * Creates a ServiceEndpointDefinition following JAX-WS rules (section 3.11) for deriving service and port names from a class containing an
     * <code>@WebService</code> annotation.
     *
     * @param annotation   the annotation
     * @param serviceClass the service class
     * @param uri          the service path
     * @return the ServiceEndpointDefinition
     */
    private ServiceEndpointDefinition createDefinition(WebService annotation, Class<?> serviceClass, URI uri) {
        String namespace = getNamespace(annotation, serviceClass);
        EndpointSynthesizerImpl.ServiceNameResult result = getServiceName(annotation, serviceClass, namespace);
        QName serviceQName = result.getServiceName();
        QName portQName = getPortName(annotation, serviceClass, namespace);
        return new ServiceEndpointDefinition(serviceQName, portQName, uri);
    }

    /**
     * Creates a ServiceEndpointDefinition following JAX-WS rules (section 3.11) for deriving default service and port names from a class.
     *
     * @param serviceClass the service class
     * @param uri          the service path
     * @return the ServiceEndpointDefinition
     */
    private ServiceEndpointDefinition createDefaultDefinition(Class<?> serviceClass, URI uri) {
        String packageName = serviceClass.getPackage().getName();
        String className = serviceClass.getSimpleName();
        String namespace = deriveNamespace(packageName);
        QName serviceQName = new QName(namespace, className + "Service");
        QName portQName = new QName(namespace, className + "Port");
        return new ServiceEndpointDefinition(serviceQName, portQName, uri);
    }

    /**
     * Returns the endpoint namespace according to JAX-WS rules.
     *
     * @param annotation   the WebService annotation on the endpoint implementation
     * @param serviceClass the endpoint implementation
     * @return the namsespace
     */
    private String getNamespace(WebService annotation, Class<?> serviceClass) {
        String namespace = annotation.targetNamespace();
        if (namespace.length() < 1) {
            String packageName = serviceClass.getPackage().getName();
            namespace = deriveNamespace(packageName);
        }
        return namespace;
    }

    /**
     * Returns the WSDL service name according to JAX-WS rules.
     *
     * @param annotation   the WebService annotation on the endpoint implementation
     * @param serviceClass the endpoint implementation
     * @param namespace    the namespace
     * @return the service name
     */
    private ServiceNameResult getServiceName(WebService annotation, Class<?> serviceClass, String namespace) {
        String serviceName = annotation.serviceName();
        boolean defaulted = serviceName.length() < 1;
        if (defaulted) {
            serviceName = serviceClass.getSimpleName() + "Service";
        }
        QName qName = new QName(namespace, serviceName);
        return new ServiceNameResult(qName, defaulted);
    }

    /**
     * Returns the WSDL port name according to JAX-WS rules.
     *
     * @param annotation   the WebService annotation on the endpoint implementation
     * @param serviceClass the endpoint implementation
     * @param namespace    the namespace
     * @return the port name
     */
    private QName getPortName(WebService annotation, Class<?> serviceClass, String namespace) {
        String portName = annotation.portName();
        if (portName.length() < 1) {
            if (annotation.name().length() < 1) {
                portName = serviceClass.getSimpleName() + "Port";
            } else {
                portName = annotation.name() + "Port";
            }
        }
        return new QName(namespace, portName);
    }

    /**
     * Returns the WSDL port type name according to JAX-WS/JSR-181 rules.
     *
     * @param annotation   the WebService annotation on the endpoint implementation
     * @param serviceClass the endpoint implementation
     * @param namespace    the namespace
     * @return the port type name
     */
    private QName getPortTypeName(WebService annotation, Class<?> serviceClass, String namespace) {
        String portTypeName = annotation.name();
        if (portTypeName.length() < 1) {
            portTypeName = serviceClass.getSimpleName();
        }
        return new QName(namespace, portTypeName);
    }

    /**
     * Derives an XML namespace from a Java package according to JAXB rules. For example, org.foo is rendered as http://foo.org/.
     *
     * @param pkg the Java package
     * @return the XML namespace
     */
    String deriveNamespace(String pkg) {
        String[] tokens = pkg.split("\\.");
        StringBuilder builder = new StringBuilder("http://");
        for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i];
            builder.append(token);
            if (i != 0) {
                builder.append(".");
            } else {
                builder.append("/");
            }
        }
        return builder.toString();
    }

    private class ServiceNameResult {
        private QName serviceName;
        private boolean defaultServiceName;

        private ServiceNameResult(QName serviceName, boolean defaultServiceName) {
            this.serviceName = serviceName;
            this.defaultServiceName = defaultServiceName;
        }

        public QName getServiceName() {
            return serviceName;
        }

        public boolean isDefaultServiceName() {
            return defaultServiceName;
        }
    }
}
