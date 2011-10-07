/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

import org.fabric3.binding.ws.model.WsBindingDefinition;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.spi.classloader.ClassLoaderObjectInputStream;
import org.fabric3.spi.generator.EffectivePolicy;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.wsdl.model.WsdlServiceContract;

/**
 * Default WsdlSynthesizer implementation.
 * <p/>
 * TODO This implementation only supports doc/lit/wrapped web services. It needs to be enhanced to support other invocation styles.
 *
 * @version $Rev: 7740 $ $Date: 2009-10-01 23:52:12 +0200 (Thu, 01 Oct 2009) $
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