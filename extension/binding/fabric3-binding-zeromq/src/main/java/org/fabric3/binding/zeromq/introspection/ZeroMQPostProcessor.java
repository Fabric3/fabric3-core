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
package org.fabric3.binding.zeromq.introspection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.api.binding.zeromq.annotation.ZeroMQ;
import org.fabric3.api.binding.zeromq.model.SocketAddressDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQBindingDefinition;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.api.model.type.component.AbstractService;
import org.fabric3.api.model.type.component.BindingDefinition;
import org.fabric3.api.model.type.component.ReferenceDefinition;
import org.fabric3.api.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.AbstractBindingPostProcessor;
import org.fabric3.spi.introspection.java.InvalidAnnotation;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Introspects ZeroMQ binding information in a component implementation.
 */
@EagerInit
public class ZeroMQPostProcessor extends AbstractBindingPostProcessor<ZeroMQ> {

    public ZeroMQPostProcessor() {
        super(ZeroMQ.class);
    }

    protected BindingDefinition processService(ZeroMQ annotation,
                                               AbstractService<?> boundService,
                                               InjectingComponentType componentType,
                                               Class<?> implClass,
                                               IntrospectionContext context) {
        try {
            Class<?> serviceInterface = implClass.getClassLoader().loadClass(boundService.getServiceContract().getQualifiedInterfaceName());

            ZeroMQMetadata metadata = new ZeroMQMetadata();
            String bindingName = "ZMQ" + serviceInterface.getSimpleName();
            ZeroMQBindingDefinition binding = new ZeroMQBindingDefinition(bindingName, metadata);

            parseAddresses(annotation, metadata, implClass, implClass, context);
            processMetadata(annotation, metadata);
            return binding;
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    protected BindingDefinition processServiceCallback(ZeroMQ annotation,
                                                       AbstractService<?> service,
                                                       InjectingComponentType componentType,
                                                       Class<?> implClass,
                                                       IntrospectionContext context) {
        return null; // not needed
    }

    protected BindingDefinition processReference(ZeroMQ annotation,
                                                 ReferenceDefinition reference,
                                                 AccessibleObject object,
                                                 Class<?> implClass,
                                                 IntrospectionContext context) {
        ZeroMQMetadata metadata = new ZeroMQMetadata();
        String bindingName = "ZMQ" + reference.getName();
        ZeroMQBindingDefinition binding = new ZeroMQBindingDefinition(bindingName, metadata);

        parseTarget(annotation, binding, object, implClass, context);
        parseAddresses(annotation, metadata, object, implClass, context);

        processMetadata(annotation, metadata);
        return binding;
    }

    protected BindingDefinition processReferenceCallback(ZeroMQ annotation,
                                                         ReferenceDefinition reference,
                                                         AccessibleObject object,
                                                         Class<?> implClass,
                                                         IntrospectionContext context) {
        return null; // not needed
    }

    private void processMetadata(ZeroMQ annotation, ZeroMQMetadata metadata) {
        metadata.setTimeout(annotation.timeout());
        metadata.setHighWater(annotation.highWater());
        metadata.setMulticastRate(annotation.multicastRate());
        metadata.setReceiveBuffer(annotation.receiveBuffer());
        metadata.setMulticastRecovery(annotation.multicastRecovery());
        metadata.setSendBuffer(annotation.sendBuffer());
        metadata.setWireFormat(annotation.wireFormat());
    }

    private void parseAddresses(ZeroMQ annotation, ZeroMQMetadata metadata, AnnotatedElement element, Class<?> implClass, IntrospectionContext context) {
        String addresses = annotation.addresses();
        if (addresses.length() == 0) {
            return;
        }
        List<SocketAddressDefinition> addressDefinitions = new ArrayList<SocketAddressDefinition>();
        String[] addressStrings = addresses.split("\\s+");
        for (String entry : addressStrings) {
            String[] tokens = entry.split(":");
            if (tokens.length != 2) {
                context.addError(new InvalidAnnotation("Invalid address specified on ZeroMQ binding: " + entry, element, annotation, implClass));
            } else {
                try {
                    String host = tokens[0];
                    int port = Integer.parseInt(tokens[1]);
                    addressDefinitions.add(new SocketAddressDefinition(host, port));
                } catch (NumberFormatException e) {
                    context.addError(new InvalidAnnotation("Invalid port specified on ZeroMQ binding: " + e.getMessage(), element, annotation, implClass));
                }
            }
        }
        metadata.setSocketAddresses(addressDefinitions);
    }

    private void parseTarget(ZeroMQ annotation, ZeroMQBindingDefinition binding, AccessibleObject object, Class<?> implClass, IntrospectionContext context) {
        String target = annotation.target();
        try {
            URI targetUri = new URI(target);
            binding.setTargetUri(targetUri);
        } catch (URISyntaxException e) {
            InvalidAnnotation error = new InvalidAnnotation("Invalid target URI specified on ZeroMQ annotation: " + target, object, annotation, implClass, e);
            context.addError(error);
        }
    }

}
