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
package org.fabric3.binding.ws.metro.generator.java.wsdl;

import java.util.Map;
import javax.xml.namespace.QName;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.model.AbstractSEIModelImpl;
import com.sun.xml.ws.model.RuntimeModeler;
import com.sun.xml.ws.wsdl.writer.WSDLGenerator;

/**
 * Default implementation of JavaWsdlGenerator that uses the Metro WSDL modeler to generate a WSDL from a Java class.
 */
public class JavaWsdlGeneratorImpl implements JavaWsdlGenerator {

    public GeneratedArtifacts generate(Class<?> seiClass, QName serviceQName, String endpointAddress, BindingID bindingId)
            throws WsdlGenerationException {
        RuntimeModeler modeler = new RuntimeModeler(seiClass, serviceQName, bindingId);
        AbstractSEIModelImpl model = modeler.buildRuntimeModel();
        GeneratedWsdlResolver wsdlResolver = new GeneratedWsdlResolver();
        WSBinding binding = BindingImpl.create(bindingId);
        WSDLGenerator generator = new WSDLGenerator(model, wsdlResolver, binding, null, seiClass, true);
        generator.setEndpointAddress(endpointAddress);
        // generate the WSDL and schemas
        generator.doGeneration();

        String wsdl = wsdlResolver.getGeneratedWsdl();
        Map<String, String> schemas = wsdlResolver.getGeneratedSchemas();
        return new GeneratedArtifacts(wsdl, schemas);
    }
}