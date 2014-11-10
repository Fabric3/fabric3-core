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

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Holder;

import com.sun.xml.ws.wsdl.writer.WSDLResolver;

/**
 * Resolves WSDL and schema artifacts for a SEI to in-memory streams.
 */
public class GeneratedWsdlResolver implements WSDLResolver {
    private String wsdlName;
    private StringWriter generatedWsdl;
    private Map<String, StringWriter> generatedSchemas;

    /**
     * Constructor.
     */
    public GeneratedWsdlResolver() {
        generatedSchemas = new HashMap<>();
        generatedWsdl = new StringWriter();
    }

    public Result getWSDL(String name) {
        wsdlName = name;
        return toResult(name, generatedWsdl);
    }

    public Result getAbstractWSDL(Holder<String> filename) {
        // force abstract and concrete WSDL elements to be merged into the same WSDL document
        return getWSDL(wsdlName);
    }

    public Result getSchemaOutput(String namespace, Holder<String> filename) {
        if (namespace.equals("")) {
            return null;
        }
        StringWriter writer = new StringWriter();
        generatedSchemas.put(filename.value, writer);
        return toResult(filename.value, writer);
    }

    public String getGeneratedWsdl() {
        return generatedWsdl.toString();
    }

    public Map<String, String> getGeneratedSchemas() {
        Map<String, String> ret = new HashMap<>();
        for (Map.Entry<String, StringWriter> entry : generatedSchemas.entrySet()) {
            ret.put(entry.getKey(), entry.getValue().toString());
        }
        return ret;
    }

    private Result toResult(String name, StringWriter writer) {
        Result result;
        result = new StreamResult(writer);
        result.setSystemId(name.replace('\\', '/'));
        return result;
    }


}