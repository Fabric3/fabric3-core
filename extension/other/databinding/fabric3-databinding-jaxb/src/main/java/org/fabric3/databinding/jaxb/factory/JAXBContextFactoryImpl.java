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
package org.fabric3.databinding.jaxb.factory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Default implementation of JAXBContextFactory.
 */
public class JAXBContextFactoryImpl implements JAXBContextFactory {

    /**
     * Constructs a JAXB context by introspecting a set of classnames.
     *
     * @param classes the context class types
     * @return a JAXB context
     * @throws JAXBException if an error occurs creating the JAXB context
     */
    public JAXBContext createJAXBContext(Class<?>... classes) throws JAXBException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader cl = getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(cl);
            if (classes == null) {
                return JAXBContext.newInstance();
            }
            return JAXBContext.newInstance(classes);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
