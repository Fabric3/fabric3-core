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
package org.fabric3.binding.ws.metro.generator.wsdl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.fabric3.api.binding.ws.model.WsBindingDefinition;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.spi.classloader.ClassLoaderObjectInputStream;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * Default WsdlSynthesizer implementation.
 * <p/>
 * TODO This implementation only supports doc/lit/wrapped web services. It needs to be enhanced to support other invocation styles.
 */
public class WsdlSynthesizerImpl implements WsdlSynthesizer {
    private static final QName SOAP_BINDING = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "binding");
    private static final QName SOAP_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address");
    private static final QName SOAP_BODY = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "body");
    private static final QName SOAP_OPERATION = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "operation");
    public static final String HTTP_TRANSPORT = "http://schemas.xmlsoap.org/soap/http";

    public ConcreteWsdlResult synthesize(LogicalBinding<WsBindingDefinition> logicalBinding,
                                          String endpointAddress,
                                          WsdlServiceContract contract,
                                          EffectivePolicy policy,
                                          Definition wsdl,
                                          URI targetUri) throws WsdlSynthesisException {

        Definition copy = clone(wsdl);

        String targetNamespace = wsdl.getTargetNamespace();
        String localName = logicalBinding.getParent().getUri().getFragment();
        // synthesize service and port names according to the SCA web services binding specification
        // TODO this does not yet take into account binding name
        QName serviceName = new QName(targetNamespace, localName);
        String localPortName = localName + "Port";

        Binding binding = copy.createBinding();
        binding.setPortType(copy.getPortType(contract.getPortType().getQName()));
        binding.setQName(new QName(targetNamespace, localName + "Binding"));
        try {
            SOAPBinding soapBinding = (SOAPBinding) copy.getExtensionRegistry().createExtension(Binding.class, SOAP_BINDING);
            soapBinding.setStyle("document");
            soapBinding.setTransportURI(HTTP_TRANSPORT);
            binding.addExtensibilityElement(soapBinding);
            for (Operation operation : contract.getOperations()) {
                BindingOperation bindingOperation = copy.createBindingOperation();
                bindingOperation.setName(operation.getName());

                // SOAP operation
                SOAPOperation soapOperation = (SOAPOperation) copy.getExtensionRegistry().createExtension(BindingOperation.class, SOAP_OPERATION);
                soapOperation.setSoapActionURI("");
                bindingOperation.addExtensibilityElement(soapOperation);

                BindingInput input = copy.createBindingInput();
                SOAPBody soapBody = (SOAPBody) copy.getExtensionRegistry().createExtension(BindingInput.class, SOAP_BODY);
                soapBody.setUse("literal");
                input.addExtensibilityElement(soapBody);
                bindingOperation.setBindingInput(input);

                BindingOutput output = copy.createBindingOutput();
                SOAPBody outSoapBody = (SOAPBody) copy.getExtensionRegistry().createExtension(BindingInput.class, SOAP_BODY);
                outSoapBody.setUse("literal");
                output.addExtensibilityElement(outSoapBody);
                bindingOperation.setBindingOutput(output);

                binding.addBindingOperation(bindingOperation);
                binding.setUndefined(false);
            }

            copy.addBinding(binding);

            QName portName = new QName(targetNamespace, localPortName);
            Port port = copy.createPort();
            port.setName(localPortName);
            port.setBinding(binding);
            SOAPAddress soapAddress = (SOAPAddress) copy.getExtensionRegistry().createExtension(Port.class, SOAP_ADDRESS);
            soapAddress.setLocationURI(endpointAddress);
            port.addExtensibilityElement(soapAddress);
            Service service = copy.createService();
            service.setQName(serviceName);
            service.addPort(port);

            copy.addService(service);

            return new ConcreteWsdlResult(copy, serviceName, portName);
        } catch (WSDLException e) {
            throw new WsdlSynthesisException(e);
        }

    }

    /**
     * Clones a parsed WSDL via serialization as <code>Definition</code> does  not implement<code>Cloneable</code>. Existing concrete WSDL information
     * will also be removed.
     *
     * @param wsdl the WSDL
     * @return the cloned WSDL
     * @throws WsdlSynthesisException if an error cloning is encountered
     */
    private Definition clone(Definition wsdl) throws WsdlSynthesisException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos);
            os.writeObject(wsdl);
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            // Use a specialized ObjectInputStream that uses an explicit classloader since the WSDL definition needs to be deserialized using a
            // classloader that has visibility to implementation classes (the Metro extension classloader does not have visibility to WSDL
            // implementation classes).
            ObjectInputStream is = new ClassLoaderObjectInputStream(bis, Definition.class.getClassLoader());
            Definition copy = (Definition) is.readObject();

            // remove concrete WSDL information
            copy.getServices().clear();
            copy.getBindings().clear();
            return copy;
        } catch (IOException e) {
            throw new WsdlSynthesisException(e);
        } catch (ClassNotFoundException e) {
            // should not happen
            throw new AssertionError(e);
        }
    }


}