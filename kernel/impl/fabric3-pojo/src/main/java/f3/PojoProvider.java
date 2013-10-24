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
