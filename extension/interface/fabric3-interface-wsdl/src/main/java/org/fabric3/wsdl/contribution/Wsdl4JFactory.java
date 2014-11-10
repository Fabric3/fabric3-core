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
package org.fabric3.wsdl.contribution;

import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

/**
 * Provides WSDLReader and WSDLWriter instances.
 */
public interface Wsdl4JFactory {

    /**
     * Registers a WSDL parser extension.
     *
     * @param parentType    the parent type of the extension element
     * @param elementType   the element type
     * @param extensionType the extension class type
     * @param serializer    the serializer
     * @param deserializer  the deserializer
     */
    void register(Class<?> parentType, QName elementType, Class<?> extensionType, ExtensionSerializer serializer, ExtensionDeserializer deserializer);

    /**
     * Removes a WSDL parser extension.
     *
     * @param parentType    the parent type of the extension element
     * @param elementType   the element type
     * @param extensionType the extension class type
     */
    void unregister(Class parentType, QName elementType, Class<?> extensionType);

    /**
     * Creates a new WSDL reader.
     *
     * @return the WSDL reader
     */
    WSDLReader newReader();

    /**
     * Creates a new WSDL writer.
     *
     * @return the WSDL writer
     */
    WSDLWriter newWriter();

}
