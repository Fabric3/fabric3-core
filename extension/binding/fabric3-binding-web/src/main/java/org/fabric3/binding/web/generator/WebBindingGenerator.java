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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.web.generator;

import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.web.model.WebBindingDefinition;
import org.fabric3.binding.web.provision.WebSourceDefinition;
import org.fabric3.binding.web.provision.WebTargetDefinition;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.fabric3.spi.policy.EffectivePolicy;

/**
 * Generates metadata for attaching a service to a websocket or comet connection.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class WebBindingGenerator implements BindingGenerator<WebBindingDefinition> {
    private static final QName XSD_ANY = new QName(XSDType.XSD_NS, "anyType");
    private static final DataType<?> XSD_TYPE = new XSDType(Object.class, XSD_ANY);
    private static final DataType<?> JSON_TYPE = new JsonType<Object>(String.class, Object.class);

    public PhysicalSourceDefinition generateSource(LogicalBinding<WebBindingDefinition> binding,
                                                   ServiceContract contract,
                                                   List<LogicalOperation> operations,
                                                   EffectivePolicy policy) throws GenerationException {
        URI uri = binding.getParent().getUri();
        String wireFormat = binding.getDefinition().getWireFormat();

        DataType<?> dataType = getDataType(wireFormat);
        return new WebSourceDefinition(uri, contract, dataType);
    }
    public PhysicalTargetDefinition generateTarget(LogicalBinding<WebBindingDefinition> binding,
                                                   ServiceContract contract,
                                                   List<LogicalOperation> operations,
                                                   EffectivePolicy policy) throws GenerationException {
        if (!binding.isCallback()) {
            throw new UnsupportedOperationException("The web binding not supported on references");
        }
        Bindable service = binding.getParent();
        String wireFormat = binding.getDefinition().getWireFormat();
        if (wireFormat == null) {
            wireFormat = introspectWireFormat(service);
        }
        DataType<?> dataType = getDataType(wireFormat);
        return new WebTargetDefinition(service.getUri(), contract, dataType);
    }

    public PhysicalTargetDefinition generateServiceBindingTarget(LogicalBinding<WebBindingDefinition> binding,
                                                                 ServiceContract contract,
                                                                 List<LogicalOperation> operations,
                                                                 EffectivePolicy policy) throws GenerationException {
        throw new UnsupportedOperationException("The web binding not supported on references");
    }

    private String introspectWireFormat(Bindable service) throws GenerationException {
        String wireFormat = null;
        boolean found = false;
        // use wire format from the forward binding on the service
        for (LogicalBinding<?> forwardBinding : service.getBindings()) {
            if (forwardBinding.getDefinition() instanceof WebBindingDefinition) {
                WebBindingDefinition definition = (WebBindingDefinition) forwardBinding.getDefinition();
                wireFormat = definition.getWireFormat();
                if (found && wireFormat != null) {
                    URI uri = service.getUri();
                    throw new GenerationException("Multiple web bindings configured on service. The wire format must be explicitly set:" + uri);
                }
                if (wireFormat != null) {
                    found = true;
                }
            }

        }
        return (wireFormat == null) ? "json" : wireFormat;
    }


    private DataType<?> getDataType(String wireFormat) {
        if ("xml".equalsIgnoreCase(wireFormat)) {
            return XSD_TYPE;
        } else {
            // default to JSON
            return JSON_TYPE;
        }
    }

}