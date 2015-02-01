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
package org.fabric3.databinding.jaxb.transform;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.spi.transform.Transformer;

/**
 * Transforms a serialized XML String to a JAXBElement.
 */
public class String2JAXBElementTransformer implements Transformer<String, Object> {
    private JAXBContext jaxbContext;
    private Class<?> declaredType;

    public String2JAXBElementTransformer(JAXBContext jaxbContext, Class<?> declaredType) {
        this.jaxbContext = jaxbContext;
        this.declaredType = declaredType;
    }

    public Object transform(String source, ClassLoader loader) throws Fabric3Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            StringReader reader = new StringReader(source);
            StreamSource streamSource = new StreamSource(reader);
            return jaxbContext.createUnmarshaller().unmarshal(streamSource, declaredType).getValue();
        } catch (JAXBException e) {
            throw new Fabric3Exception(e);
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

}