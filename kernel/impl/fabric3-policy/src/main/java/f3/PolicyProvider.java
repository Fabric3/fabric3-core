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
