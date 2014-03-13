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

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.fabric3.api.model.type.contract.DataType;
import org.fabric3.api.model.type.contract.Operation;
import org.fabric3.databinding.jaxb.mapper.JAXBQNameMapper;
import org.fabric3.databinding.jaxb.mapper.JAXBQNameMapperImpl;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.model.type.java.JavaType;

/**
 *
 */
public class JAXBTypeIntrospectorTestCase extends TestCase {
    private static final QName XSD_INT = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "int");
    private JAXBTypeIntrospector introspector;

    public void testDefaultMapping() throws Exception {
        Operation operation = createOperation("operation", int.class);
        introspector.introspect(operation, null, new DefaultIntrospectionContext());
        DataType dataType = operation.getInputTypes().get(0);
        assertEquals(XSD_INT, dataType.getXsdType());
    }

    protected void setUp() throws Exception {
        super.setUp();
        JAXBQNameMapper mapper = new JAXBQNameMapperImpl();
        introspector = new JAXBTypeIntrospector(mapper);
    }

    @SuppressWarnings({"unchecked"})
    private Operation createOperation(String name, Class<?> paramType) {
        JavaType type = new JavaType(paramType);
        List<DataType> in = new ArrayList<>();
        in.add(type);
        JavaType outputType = new JavaType(Void.class);
        return new Operation(name, in, outputType, null);
    }

    @XmlRootElement
    private class Param {

    }

}
