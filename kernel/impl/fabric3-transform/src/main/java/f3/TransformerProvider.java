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
import static org.fabric3.spi.model.type.system.SystemComponentBuilder.newBuilder;

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
