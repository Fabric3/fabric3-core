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
import org.fabric3.implementation.system.generator.SystemComponentGenerator;
import org.fabric3.implementation.system.introspection.SystemConstructorHeuristic;
import org.fabric3.implementation.system.introspection.SystemHeuristic;
import org.fabric3.implementation.system.introspection.SystemImplementationIntrospectorImpl;
import org.fabric3.implementation.system.introspection.SystemImplementationLoader;
import org.fabric3.implementation.system.introspection.SystemServiceHeuristic;
import org.fabric3.implementation.system.introspection.SystemUnannotatedHeuristic;
import org.fabric3.implementation.system.provision.SystemComponentDefinition;
import org.fabric3.implementation.system.provision.SystemConnectionSourceDefinition;
import org.fabric3.implementation.system.provision.SystemConnectionTargetDefinition;
import org.fabric3.implementation.system.provision.SystemSourceDefinition;
import org.fabric3.implementation.system.provision.SystemTargetDefinition;
import org.fabric3.implementation.system.runtime.SystemComponentBuilder;
import org.fabric3.implementation.system.runtime.SystemSourceConnectionAttacher;
import org.fabric3.implementation.system.runtime.SystemSourceWireAttacher;
import org.fabric3.implementation.system.runtime.SystemTargetConnectionAttacher;
import org.fabric3.implementation.system.runtime.SystemTargetWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonComponentGenerator;
import org.fabric3.implementation.system.singleton.SingletonImplementation;
import org.fabric3.implementation.system.singleton.SingletonSourceDefinition;
import org.fabric3.implementation.system.singleton.SingletonSourceWireAttacher;
import org.fabric3.implementation.system.singleton.SingletonTargetDefinition;
import org.fabric3.implementation.system.singleton.SingletonTargetWireAttacher;
import org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder;
import org.fabric3.spi.model.type.system.SystemImplementation;
import static org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder.newBuilder;

/**
 * Provides components for managing system and singleton components.
 */
public class SystemImplementationProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "SystemImplementationComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        addSystemImplementation(compositeBuilder);
        addSingletonImplementation(compositeBuilder);

        return compositeBuilder.build();
    }

    private static void addSingletonImplementation(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(SingletonComponentGenerator.class).key(SingletonImplementation.class.getName()).build());
        compositeBuilder.component(newBuilder(SingletonSourceWireAttacher.class).key(SingletonSourceDefinition.class.getName()).build());
        compositeBuilder.component(newBuilder(SingletonTargetWireAttacher.class).key(SingletonTargetDefinition.class.getName()).build());
    }

    private static void addSystemImplementation(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(SystemImplementationLoader.class).key(Namespaces.F3_PREFIX + "implementation.system").build());
        compositeBuilder.component(newBuilder(SystemComponentBuilder.class).key(SystemComponentDefinition.class.getName()).build());
        compositeBuilder.component(newBuilder(SystemSourceWireAttacher.class).key(SystemSourceDefinition.class.getName()).build());
        compositeBuilder.component(newBuilder(SystemTargetWireAttacher.class).key(SystemTargetDefinition.class.getName()).build());
        compositeBuilder.component(newBuilder(SystemSourceConnectionAttacher.class).key(SystemConnectionSourceDefinition.class.getName()).build());
        compositeBuilder.component(newBuilder(SystemTargetConnectionAttacher.class).key(SystemConnectionTargetDefinition.class.getName()).build());

        SystemComponentDefinitionBuilder componentBuilder = newBuilder("SystemImplementationIntrospectorImpl", SystemImplementationIntrospectorImpl.class);
        componentBuilder.key(Namespaces.F3_PREFIX + "implementation.system");
        componentBuilder.reference("heuristic", "SystemHeuristic");
        compositeBuilder.component(componentBuilder.build());

        componentBuilder = newBuilder(SystemHeuristic.class);
        componentBuilder.reference("service", "SystemServiceHeuristic");
        componentBuilder.reference("constructor", "SystemConstructorHeuristic");
        componentBuilder.reference("injection", "SystemUnannotatedHeuristic");
        compositeBuilder.component(componentBuilder.build());

        compositeBuilder.component(newBuilder(SystemServiceHeuristic.class).build());
        compositeBuilder.component(newBuilder(SystemConstructorHeuristic.class).build());
        compositeBuilder.component(newBuilder(SystemUnannotatedHeuristic.class).build());

        compositeBuilder.component(newBuilder(SystemComponentGenerator.class).key(SystemImplementation.class.getName()).build());
    }

}
