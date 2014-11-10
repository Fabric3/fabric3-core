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
import org.fabric3.implementation.pojo.builder.ArrayBuilderImpl;
import org.fabric3.implementation.pojo.builder.CollectionBuilderImpl;
import org.fabric3.implementation.pojo.builder.MapBuilderImpl;
import org.fabric3.implementation.pojo.builder.ObjectBuilderImpl;
import org.fabric3.implementation.pojo.builder.PropertyObjectFactoryBuilderImpl;
import org.fabric3.implementation.pojo.generator.GenerationHelperImpl;
import org.fabric3.implementation.pojo.manager.ImplementationManagerFactoryBuilderImpl;
import org.fabric3.implementation.pojo.provision.ImplementationManagerDefinition;
import org.fabric3.implementation.pojo.proxy.ChannelProxyServiceImpl;
import org.fabric3.implementation.pojo.proxy.WireProxyServiceImpl;
import org.fabric3.implementation.pojo.reflection.ReflectionFactoryImpl;
import static org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder.newBuilder;

/**
 * Provides components for handling POJO runtime artifacts.
 */
public class PojoProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "PojoComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        compositeBuilder.component(newBuilder(ImplementationManagerFactoryBuilderImpl.class).key(ImplementationManagerDefinition.class.getName()).build());
        compositeBuilder.component(newBuilder(ReflectionFactoryImpl.class).build());
        compositeBuilder.component(newBuilder(GenerationHelperImpl.class).build());
        compositeBuilder.component(newBuilder(PropertyObjectFactoryBuilderImpl.class).build());
        compositeBuilder.component(newBuilder(ArrayBuilderImpl.class).build());
        compositeBuilder.component(newBuilder(CollectionBuilderImpl.class).build());
        compositeBuilder.component(newBuilder(MapBuilderImpl.class).build());
        compositeBuilder.component(newBuilder(ObjectBuilderImpl.class).build());
        compositeBuilder.component(newBuilder(ChannelProxyServiceImpl.class).build());
        compositeBuilder.component(newBuilder(WireProxyServiceImpl.class).build());

        return compositeBuilder.build();
    }

}
