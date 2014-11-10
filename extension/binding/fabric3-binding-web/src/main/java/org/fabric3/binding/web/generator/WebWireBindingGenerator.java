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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.web.generator;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.ServiceContract;
import org.fabric3.binding.web.model.WebBindingDefinition;
import org.fabric3.binding.web.provision.WebWireSourceDefinition;
import org.fabric3.binding.web.provision.WebWireTargetDefinition;
import org.fabric3.spi.domain.generator.GenerationException;
import org.fabric3.spi.domain.generator.wire.WireBindingGenerator;
import org.fabric3.spi.domain.generator.policy.EffectivePolicy;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.model.type.xsd.XSDType;
import org.oasisopen.sca.annotation.EagerInit;

/**
 * Generates metadata for attaching a service to a websocket or comet connection.
 */
@EagerInit
public class WebWireBindingGenerator implements WireBindingGenerator<WebBindingDefinition> {
    private static final QName XSD_ANY = new QName(XSDType.XSD_NS, "anyType");
    private static final DataType XSD_TYPE = new XSDType(Object.class, XSD_ANY);
    private static final DataType JSON_TYPE = new JsonType(String.class);

    public PhysicalWireSourceDefinition generateSource(LogicalBinding<WebBindingDefinition> binding,
                                                   ServiceContract contract,
                                                   List<LogicalOperation> operations,
                                                   EffectivePolicy policy) throws GenerationException {
        URI uri = binding.getParent().getUri();
        String wireFormat = binding.getDefinition().getWireFormat();

        DataType dataType = getDataType(wireFormat);
        return new WebWireSourceDefinition(uri, contract, dataType);
    }
    public PhysicalWireTargetDefinition generateTarget(LogicalBinding<WebBindingDefinition> binding,
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
        DataType dataType = getDataType(wireFormat);
        return new WebWireTargetDefinition(service.getUri(), dataType);
    }

    public PhysicalWireTargetDefinition generateServiceBindingTarget(LogicalBinding<WebBindingDefinition> binding,
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


    private DataType getDataType(String wireFormat) {
        if ("xml".equalsIgnoreCase(wireFormat)) {
            return XSD_TYPE;
        } else {
            // default to JSON
            return JSON_TYPE;
        }
    }

}