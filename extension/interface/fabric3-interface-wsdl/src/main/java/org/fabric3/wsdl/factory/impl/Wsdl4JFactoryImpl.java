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
package org.fabric3.wsdl.factory.impl;

import java.util.ArrayList;
import java.util.List;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionDeserializer;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.ExtensionSerializer;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.fabric3.wsdl.factory.Wsdl4JFactory;

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
        WSDLWriter writer = factory.newWSDLWriter();
//        writer.setFeature(VERBOSE, false);
        return writer;
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