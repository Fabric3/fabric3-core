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
package org.fabric3.wsdl.contribution.impl;

import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.wsdl.contribution.Wsdl4JFactory;

/**
 * An implementation of WsdlFactory that accepts registration and deregistration of WSDLReader and WSDLWriter extensions.
 */
public class Wsdl4JFactoryImpl implements Wsdl4JFactory {
    private static final String VERBOSE = "javax.wsdl.verbose";
    private WSDLFactory factory;
    private List<Holder> holders = new ArrayList<>();

    public Wsdl4JFactoryImpl() throws WSDLException {
        factory = WSDLFactory.newInstance();
    }

    public void register(Class<?> parentType,
                         QName elementType,
                         Class<?> extensionType,
                         ExtensionSerializer serializer,
                         ExtensionDeserializer deserializer) {
        Holder holder = new Holder(parentType, elementType, extensionType, deserializer, serializer);
        holders.add(holder);
    }

    public void unregister(Class parentType, QName elementType, Class<?> extensionType) {
        Holder holder = new Holder(parentType, elementType, extensionType, null, null);
        holders.remove(holder);
    }


    public WSDLReader newReader() {
        WSDLReader reader = factory.newWSDLReader();
        reader.setFeature(VERBOSE, false);
        ExtensionRegistry registry = createRegistry();
        reader.setExtensionRegistry(registry);
        return reader;
    }

    public WSDLWriter newWriter() {
        //        writer.setFeature(VERBOSE, false);
        return factory.newWSDLWriter();
    }

    /**
     * Creates a registry populated with registered extensions.
     *
     * @return the registry
     */
    private ExtensionRegistry createRegistry() {
        ExtensionRegistry registry = factory.newPopulatedExtensionRegistry();
        for (Holder holder : holders) {
            Class parentType = holder.getParentType();
            QName elementType = holder.getElementType();
            Class<?> extensionType = holder.getExtensionType();
            ExtensionDeserializer deserializer = holder.getDeserializer();
            ExtensionSerializer serializer = holder.getSerializer();
            registry.registerDeserializer(parentType, elementType, deserializer);
            registry.registerSerializer(parentType, elementType, serializer);
            registry.mapExtensionTypes(holder.getParentType(), holder.getElementType(), extensionType);
        }
        return registry;
    }

    /**
     * Holds registered extensions. Equals is implemented to perform a comparison on parentType, elementType and extensionType to the holder can be
     * used as a query key.
     */
    private class Holder {
        private Class<?> parentType;
        private QName elementType;
        private Class<?> extensionType;
        private ExtensionDeserializer deserializer;
        private ExtensionSerializer serializer;

        private Holder(Class<?> parentType,
                       QName elementType,
                       Class<?> extensionType,
                       ExtensionDeserializer deserializer,
                       ExtensionSerializer serializer) {
            this.parentType = parentType;
            this.elementType = elementType;
            this.extensionType = extensionType;
            this.deserializer = deserializer;
            this.serializer = serializer;
        }

        public Class getParentType() {
            return parentType;
        }

        public QName getElementType() {
            return elementType;
        }

        public Class<?> getExtensionType() {
            return extensionType;
        }

        public ExtensionDeserializer getDeserializer() {
            return deserializer;
        }

        public ExtensionSerializer getSerializer() {
            return serializer;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Holder holder = (Holder) o;

            return !(elementType != null ? !elementType.equals(holder.elementType) : holder.elementType != null) &&
                    !(extensionType != null ? !extensionType.equals(holder.extensionType) : holder.extensionType != null) &&
                    !(parentType != null ? !parentType.equals(holder.parentType) : holder.parentType != null);

        }

        @Override
        public int hashCode() {
            int result = parentType != null ? parentType.hashCode() : 0;
            result = 31 * result + (elementType != null ? elementType.hashCode() : 0);
            result = 31 * result + (extensionType != null ? extensionType.hashCode() : 0);
            result = 31 * result + (deserializer != null ? deserializer.hashCode() : 0);
            result = 31 * result + (serializer != null ? serializer.hashCode() : 0);
            return result;
        }
    }
}