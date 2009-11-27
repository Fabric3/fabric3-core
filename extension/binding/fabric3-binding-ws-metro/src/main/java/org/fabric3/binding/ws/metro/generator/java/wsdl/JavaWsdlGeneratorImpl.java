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
 *
 * @version $Rev$ $Date$
 */
public class JavaWsdlGeneratorImpl implements JavaWsdlGenerator {

    public GeneratedArtifacts generate(Class<?> seiClass, QName serviceQName, String endpointAddress, BindingID bindingId)
            throws WsdlGenerationException {
        RuntimeModeler modeler = new RuntimeModeler(seiClass, serviceQName, bindingId);
        AbstractSEIModelImpl model = modeler.buildRuntimeModel();
        GeneratedWsdlResolver wsdlResolver = new GeneratedWsdlResolver();
        WSBinding binding = BindingImpl.create(bindingId);
        WSDLGenerator generator = new WSDLGenerator(model, wsdlResolver, binding, null, seiClass);
        generator.setEndpointAddress(endpointAddress);
        // generate the WSDL and schemas
        generator.doGeneration();

        String wsdl = wsdlResolver.getGeneratedWsdl();
        Map<String, String> schemas = wsdlResolver.getGeneratedSchemas();
        return new GeneratedArtifacts(wsdl, schemas);
    }
}