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
package f3;

import javax.xml.namespace.QName;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.transform.DefaultTransformerRegistry;
import org.fabric3.transform.binary.ByteArrayToByteArrayTransformer;
import org.fabric3.transform.binary.TwoDimensionByteArrayTransformer;
import org.fabric3.transform.java.Bytes2JavaTransformerFactory;
import org.fabric3.transform.java.Java2BytesTransformerFactory;
import org.fabric3.transform.java.Java2JavaTransformerFactory;
import org.fabric3.transform.property.Property2BooleanTransformer;
import org.fabric3.transform.property.Property2ByteTransformer;
import org.fabric3.transform.property.Property2CalendarTransformer;
import org.fabric3.transform.property.Property2ClassTransformer;
import org.fabric3.transform.property.Property2DateTransformer;
import org.fabric3.transform.property.Property2DoubleTransformer;
import org.fabric3.transform.property.Property2ElementTransformer;
import org.fabric3.transform.property.Property2FloatTransformer;
import org.fabric3.transform.property.Property2IntegerTransformer;
import org.fabric3.transform.property.Property2LongTransformer;
import org.fabric3.transform.property.Property2PropertiesTransformer;
import org.fabric3.transform.property.Property2QNameTransformer;
import org.fabric3.transform.property.Property2ShortTransformer;
import org.fabric3.transform.property.Property2StreamTransformer;
import org.fabric3.transform.property.Property2StringTransformer;
import org.fabric3.transform.property.Property2URITransformer;
import org.fabric3.transform.property.Property2URLTransformer;
import org.fabric3.transform.string2java.String2ClassTransformer;
import org.fabric3.transform.string2java.String2IntegerTransformer;
import org.fabric3.transform.string2java.String2QNameTransformer;
import static org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder.newBuilder;

/**
 * Provides transformers.
 */
public class TransformerProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "TransformComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        compositeBuilder.component(newBuilder(DefaultTransformerRegistry.class).build());
        compositeBuilder.component(newBuilder(Property2BooleanTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2ByteTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2ShortTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2IntegerTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2LongTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2FloatTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2DoubleTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2QNameTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2ClassTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2DateTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2CalendarTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2StringTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2ElementTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2URITransformer.class).build());
        compositeBuilder.component(newBuilder(Property2URLTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2PropertiesTransformer.class).build());
        compositeBuilder.component(newBuilder(Property2StreamTransformer.class).build());
        compositeBuilder.component(newBuilder(String2QNameTransformer.class).build());
        compositeBuilder.component(newBuilder(String2ClassTransformer.class).build());
        compositeBuilder.component(newBuilder(String2IntegerTransformer.class).build());
        compositeBuilder.component(newBuilder(Java2JavaTransformerFactory.class).build());
        compositeBuilder.component(newBuilder(Java2BytesTransformerFactory.class).build());
        compositeBuilder.component(newBuilder(Bytes2JavaTransformerFactory.class).build());
        compositeBuilder.component(newBuilder(ByteArrayToByteArrayTransformer.class).build());
        compositeBuilder.component(newBuilder(TwoDimensionByteArrayTransformer.class).build());

        return compositeBuilder.build();
    }

}
