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
import java.util.HashMap;
import java.util.Map;

import org.fabric3.api.Namespaces;
import org.fabric3.api.annotation.Consumer;
import org.fabric3.api.annotation.Producer;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.model.Binding;
import org.fabric3.api.annotation.model.Provides;
import org.fabric3.api.annotation.runtime.DataDirectory;
import org.fabric3.api.annotation.scope.Domain;
import org.fabric3.api.annotation.scope.Stateless;
import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.annotation.wire.Order;
import org.fabric3.api.model.type.builder.CompositeBuilder;
import org.fabric3.api.model.type.component.Composite;
import org.fabric3.introspection.java.ComponentProcessorImpl;
import org.fabric3.introspection.java.DefaultClassVisitor;
import org.fabric3.introspection.java.DefaultIntrospectionHelper;
import org.fabric3.introspection.java.ReferenceProcessorImpl;
import org.fabric3.introspection.java.annotation.CompositeProcessor;
import org.fabric3.introspection.java.annotation.ConsumerProcessor;
import org.fabric3.introspection.java.annotation.DataDirectoryProcessor;
import org.fabric3.introspection.java.annotation.DomainProcessor;
import org.fabric3.introspection.java.annotation.ImplicitBindingReferenceProcessor;
import org.fabric3.introspection.java.annotation.KeyProcessor;
import org.fabric3.introspection.java.annotation.ManagementOperationProcessor;
import org.fabric3.introspection.java.annotation.ManagementProcessor;
import org.fabric3.introspection.java.annotation.OASISCallbackProcessor;
import org.fabric3.introspection.java.annotation.OASISContextProcessor;
import org.fabric3.introspection.java.annotation.OASISDestroyProcessor;
import org.fabric3.introspection.java.annotation.OASISEagerInitProcessor;
import org.fabric3.introspection.java.annotation.OASISInitProcessor;
import org.fabric3.introspection.java.annotation.OASISPropertyProcessor;
import org.fabric3.introspection.java.annotation.OASISReferenceProcessor;
import org.fabric3.introspection.java.annotation.OASISRemotableProcessor;
import org.fabric3.introspection.java.annotation.OASISScopeProcessor;
import org.fabric3.introspection.java.annotation.OASISServiceProcessor;
import org.fabric3.introspection.java.annotation.OrderProcessor;
import org.fabric3.introspection.java.annotation.PolicyAnnotationProcessorImpl;
import org.fabric3.introspection.java.annotation.ProducerProcessor;
import org.fabric3.introspection.java.annotation.StatelessProcessor;
import org.fabric3.introspection.java.contract.JavaContractProcessorImpl;
import org.fabric3.introspection.java.policy.DefaultOperationPolicyIntrospector;
import org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder;
import org.oasisopen.sca.annotation.AllowsPassByReference;
import org.oasisopen.sca.annotation.Callback;
import org.oasisopen.sca.annotation.Context;
import org.oasisopen.sca.annotation.Destroy;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Init;
import org.oasisopen.sca.annotation.Property;
import org.oasisopen.sca.annotation.Reference;
import org.oasisopen.sca.annotation.Remotable;
import org.oasisopen.sca.annotation.Scope;
import org.oasisopen.sca.annotation.Service;
import static org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder.newBuilder;

/**
 * Provides components for Java artifact introspection.
 */
public class JavaIntrospectionProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "JavaIntrospectionComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        compositeBuilder.component(newBuilder(DefaultIntrospectionHelper.class).build());

        compositeBuilder.component(newBuilder(ComponentProcessorImpl.class).build());

        compositeBuilder.component(newBuilder(JavaContractProcessorImpl.class).build());

        compositeBuilder.component(newBuilder(DefaultClassVisitor.class).build());

        compositeBuilder.component(newBuilder(DefaultOperationPolicyIntrospector.class).build());

        compositeBuilder.component(newBuilder(ReferenceProcessorImpl.class).build());

        addOASISAnnotations(compositeBuilder);
        addF3Annotations(compositeBuilder);

        return compositeBuilder.build();
    }

    private static void addF3Annotations(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(DomainProcessor.class).key(Domain.class.getName()).build());

        compositeBuilder.component(newBuilder(CompositeProcessor.class).key(org.fabric3.api.annotation.scope.Composite.class.getName()).build());

        compositeBuilder.component(newBuilder(StatelessProcessor.class).key(Stateless.class.getName()).build());

        compositeBuilder.component(newBuilder(ProducerProcessor.class).key(Producer.class.getName()).build());

        compositeBuilder.component(newBuilder(ConsumerProcessor.class).key(Consumer.class.getName()).build());

        SystemComponentDefinitionBuilder componentBuilder = newBuilder(PolicyAnnotationProcessorImpl.class);
        Map<String, QName> intents = new HashMap<>();
        intents.put(AllowsPassByReference.class.getName(), QName.valueOf(Namespaces.F3_PREFIX + "allowsPassByReference"));
        componentBuilder.property("intentsToQualifiers", intents);

        compositeBuilder.component(newBuilder(ManagementProcessor.class).key(Management.class.getName()).build());

        compositeBuilder.component(newBuilder(ManagementOperationProcessor.class).key(ManagementOperation.class.getName()).build());

        compositeBuilder.component(newBuilder(KeyProcessor.class).key(Key.class.getName()).build());

        compositeBuilder.component(newBuilder(OrderProcessor.class).key(Order.class.getName()).build());

        compositeBuilder.component(newBuilder(ImplicitBindingReferenceProcessor.class).key(Binding.class.getName()).build());

        compositeBuilder.component(newBuilder(DataDirectoryProcessor.class).key(DataDirectory.class.getName()).build());

        compositeBuilder.component(componentBuilder.build());

    }

    private static void addOASISAnnotations(CompositeBuilder compositeBuilder) {
        compositeBuilder.component(newBuilder(OASISCallbackProcessor.class).key(Callback.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISContextProcessor.class).key(Context.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISDestroyProcessor.class).key(Destroy.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISEagerInitProcessor.class).key(EagerInit.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISInitProcessor.class).key(Init.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISPropertyProcessor.class).key(Property.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISReferenceProcessor.class).key(Reference.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISRemotableProcessor.class).key(Remotable.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISScopeProcessor.class).key(Scope.class.getName()).build());

        compositeBuilder.component(newBuilder(OASISServiceProcessor.class).key(Service.class.getName()).build());

    }
}
