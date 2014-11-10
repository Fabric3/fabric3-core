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
import org.fabric3.policy.DefaultPolicyAttacher;
import org.fabric3.policy.DefaultPolicyResolver;
import org.fabric3.policy.infoset.PolicyEvaluatorImpl;
import org.fabric3.policy.interceptor.simple.SimpleInterceptorBuilder;
import org.fabric3.policy.interceptor.simple.SimpleInterceptorDefinition;
import org.fabric3.policy.interceptor.simple.SimpleInterceptorGenerator;
import org.fabric3.policy.resolver.ImplementationPolicyResolverImpl;
import org.fabric3.policy.resolver.InteractionPolicyResolverImpl;
import org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder;
import static org.fabric3.spi.model.type.system.SystemComponentDefinitionBuilder.newBuilder;

/**
 * Provides policy components.
 */
public class PolicyProvider {

    private static final QName QNAME = new QName(Namespaces.F3, "PolicyComposite");

    @Provides
    public static Composite getComposite() {
        CompositeBuilder compositeBuilder = CompositeBuilder.newBuilder(QNAME);

        compositeBuilder.component(newBuilder(PolicyEvaluatorImpl.class).build());
        compositeBuilder.component(newBuilder(InteractionPolicyResolverImpl.class).build());
        compositeBuilder.component(newBuilder(ImplementationPolicyResolverImpl.class).build());
        compositeBuilder.component(newBuilder(DefaultPolicyResolver.class).build());
        compositeBuilder.component(newBuilder(DefaultPolicyAttacher.class).build());

        compositeBuilder.component(newBuilder(SimpleInterceptorBuilder.class).key(SimpleInterceptorDefinition.class.getName()).build());
        SystemComponentDefinitionBuilder componentBuilder = newBuilder("SimpleInterceptorGenerator", SimpleInterceptorGenerator.class);
        componentBuilder.key(Namespaces.F3_PREFIX + "interceptor");
        compositeBuilder.component(componentBuilder.build());

        return compositeBuilder.build();
    }

}
