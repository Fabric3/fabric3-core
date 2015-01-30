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
import org.fabric3.api.annotation.Consumer;
import org.fabric3.api.annotation.Producer;
import org.fabric3.api.annotation.management.Management;
import org.fabric3.api.annotation.management.ManagementOperation;
import org.fabric3.api.annotation.model.Binding;
import org.fabric3.api.annotation.model.Provides;
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
import static org.fabric3.spi.model.type.system.SystemComponentBuilder.newBuilder;

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

        compositeBuilder.component(newBuilder(PolicyAnnotationProcessorImpl.class).build());

        compositeBuilder.component(newBuilder(ManagementProcessor.class).key(Management.class.getName()).build());

        compositeBuilder.component(newBuilder(ManagementOperationProcessor.class).key(ManagementOperation.class.getName()).build());

        compositeBuilder.component(newBuilder(KeyProcessor.class).key(Key.class.getName()).build());

        compositeBuilder.component(newBuilder(OrderProcessor.class).key(Order.class.getName()).build());

        compositeBuilder.component(newBuilder(ImplicitBindingReferenceProcessor.class).key(Binding.class.getName()).build());

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
