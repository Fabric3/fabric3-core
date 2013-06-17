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
package org.fabric3.databinding.jaxb.introspection;

import java.awt.*;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.databinding.jaxb.mapper.JAXBQNameMapper;
import org.fabric3.model.type.contract.DataType;
import org.fabric3.model.type.contract.Operation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.contract.OperationIntrospector;
import org.fabric3.spi.model.type.java.JavaType;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;

/**
 * Introspects operations for the presence of JAXB types. If a parameter is a JAXB type, the JAXB intent is added to the operation.
 */
public class JAXBTypeIntrospector implements OperationIntrospector {
    private static final String DEFAULT = "##default";
    private static final Map<Class, QName> JAXB_MAPPING;

    static {
        JAXB_MAPPING = new IdentityHashMap<Class, QName>();
        JAXB_MAPPING.put(Boolean.TYPE, new QName(W3C_XML_SCHEMA_NS_URI, "boolean"));
        JAXB_MAPPING.put(Byte.TYPE, new QName(W3C_XML_SCHEMA_NS_URI, "byte"));
        JAXB_MAPPING.put(Short.TYPE, new QName(W3C_XML_SCHEMA_NS_URI, "short"));
        JAXB_MAPPING.put(Integer.TYPE, new QName(W3C_XML_SCHEMA_NS_URI, "int"));
        JAXB_MAPPING.put(Long.TYPE, new QName(W3C_XML_SCHEMA_NS_URI, "long"));
        JAXB_MAPPING.put(Float.TYPE, new QName(W3C_XML_SCHEMA_NS_URI, "float"));
        JAXB_MAPPING.put(Double.TYPE, new QName(W3C_XML_SCHEMA_NS_URI, "double"));
        JAXB_MAPPING.put(Boolean.class, new QName(W3C_XML_SCHEMA_NS_URI, "boolean"));
        JAXB_MAPPING.put(Byte.class, new QName(W3C_XML_SCHEMA_NS_URI, "byte"));
        JAXB_MAPPING.put(Short.class, new QName(W3C_XML_SCHEMA_NS_URI, "short"));
        JAXB_MAPPING.put(Integer.class, new QName(W3C_XML_SCHEMA_NS_URI, "int"));
        JAXB_MAPPING.put(Long.class, new QName(W3C_XML_SCHEMA_NS_URI, "long"));
        JAXB_MAPPING.put(Float.class, new QName(W3C_XML_SCHEMA_NS_URI, "float"));
        JAXB_MAPPING.put(Double.class, new QName(W3C_XML_SCHEMA_NS_URI, "double"));
        JAXB_MAPPING.put(String.class, new QName(W3C_XML_SCHEMA_NS_URI, "string"));
        JAXB_MAPPING.put(BigInteger.class, new QName(W3C_XML_SCHEMA_NS_URI, "integer"));
        JAXB_MAPPING.put(BigDecimal.class, new QName(W3C_XML_SCHEMA_NS_URI, "decimal"));
        JAXB_MAPPING.put(Calendar.class, new QName(W3C_XML_SCHEMA_NS_URI, "dateTime"));
        JAXB_MAPPING.put(Date.class, new QName(W3C_XML_SCHEMA_NS_URI, "dateTime"));
        JAXB_MAPPING.put(QName.class, new QName(W3C_XML_SCHEMA_NS_URI, "QName"));
        JAXB_MAPPING.put(URI.class, new QName(W3C_XML_SCHEMA_NS_URI, "string"));
        JAXB_MAPPING.put(XMLGregorianCalendar.class, new QName(W3C_XML_SCHEMA_NS_URI, "anySimpleType"));
        JAXB_MAPPING.put(Duration.class, new QName(W3C_XML_SCHEMA_NS_URI, "duration"));
        JAXB_MAPPING.put(Object.class, new QName(W3C_XML_SCHEMA_NS_URI, "anyType"));
        JAXB_MAPPING.put(Image.class, new QName(W3C_XML_SCHEMA_NS_URI, "base64Binary"));
        JAXB_MAPPING.put(javax.activation.DataHandler.class, new QName(W3C_XML_SCHEMA_NS_URI, "base64Binary"));
        JAXB_MAPPING.put(Source.class, new QName(W3C_XML_SCHEMA_NS_URI, "base64Binary"));
        JAXB_MAPPING.put(UUID.class, new QName(W3C_XML_SCHEMA_NS_URI, "string"));
        JAXB_MAPPING.put(byte[].class, new QName(W3C_XML_SCHEMA_NS_URI, "base64Binary"));
    }

    private JAXBQNameMapper mapper;

    public JAXBTypeIntrospector(@Reference JAXBQNameMapper mapper) {
        this.mapper = mapper;
    }

    public void introspect(Operation operation, Method method, IntrospectionContext context) {
        // TODO perform error checking, e.g. mixing of databindings
        List<DataType<?>> inputTypes = operation.getInputTypes();
        for (DataType<?> type : inputTypes) {
            if (!(type instanceof JavaType)) {
                // programming error
                throw new AssertionError("Java contracts must use " + JavaType.class);
            }
            introspectJAXB((JavaType<?>) type);
        }
        for (DataType<?> type : operation.getFaultTypes()) {
            // FIXME need to process fault beans
            if (!(type instanceof JavaType)) {
                // programming error
                throw new AssertionError("Java contracts must use " + JavaType.class);
            }
            introspectJAXB((JavaType<?>) type);
        }
        DataType<?> outputType = operation.getOutputType();
        if (!(outputType instanceof JavaType)) {
            // programming error
            throw new AssertionError("Java contracts must use " + JavaType.class);
        }
        introspectJAXB((JavaType<?>) outputType);

    }

    private void introspectJAXB(JavaType<?> dataType) {
        Class<?> physical = dataType.getPhysical();
        // not an explicit JAXB type, but it can potentially be mapped
        QName xsdName = JAXB_MAPPING.get(physical);
        if (xsdName != null) {
            dataType.setXsdType(xsdName);
            return;
        }
        XmlRootElement annotation = physical.getAnnotation(XmlRootElement.class);
        if (annotation != null) {
            String namespace = annotation.namespace();
            if (DEFAULT.equals(namespace)) {
                namespace = getDefaultNamespace(physical);
            }
            String name = annotation.name();
            if (DEFAULT.equals(namespace)) {
                // as per the JAXB specification
                name = Introspector.decapitalize(physical.getSimpleName());
            }
            xsdName = new QName(namespace, name);
            dataType.setXsdType(xsdName);
            return;
        }
        XmlType typeAnnotation = physical.getAnnotation(XmlType.class);
        if (typeAnnotation != null) {
            String namespace = typeAnnotation.namespace();
            if (DEFAULT.equals(namespace)) {
                namespace = getDefaultNamespace(physical);
            }
            String name = typeAnnotation.name();
            if (DEFAULT.equals(namespace)) {
                // as per the JAXB specification
                name = Introspector.decapitalize(physical.getSimpleName());
            }
            xsdName = new QName(namespace, name);
            dataType.setXsdType(xsdName);
            return;
        }
        // the type is an unannotated Java class, heuristically determine a schema mapping
        xsdName = mapper.deriveQName(physical);
        dataType.setXsdType(xsdName);
    }

    private String getDefaultNamespace(Class clazz) {
        Package pkg = clazz.getPackage();
        // as per the JAXB specification
        if (pkg != null) {
            XmlSchema schemaAnnotation = pkg.getAnnotation(XmlSchema.class);
            if (schemaAnnotation != null) {
                return schemaAnnotation.namespace();
            }
            return pkg.getName();
        }
        return "";
    }
}
