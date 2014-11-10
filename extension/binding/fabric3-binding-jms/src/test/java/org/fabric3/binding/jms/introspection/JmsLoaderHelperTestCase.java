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
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.binding.jms.introspection;

import junit.framework.TestCase;
import org.fabric3.api.binding.jms.model.DeliveryMode;
import org.fabric3.api.binding.jms.model.JmsBindingMetadata;

public class JmsLoaderHelperTestCase extends TestCase {

    public void testParse() throws Exception {
        String uri = "jms:destination?jndiConnectionFactoryName=factory&deliveryMode=nonpersistent&selector=select";
        JmsBindingMetadata metadata = JmsLoaderHelper.parseUri(uri);
        assertEquals("destination", metadata.getDestination().getName());
        assertEquals("factory", metadata.getConnectionFactory().getName());
        assertEquals("select", metadata.getUriMessageSelection().getSelector());
        assertEquals(DeliveryMode.NON_PERSISTENT, metadata.getUriHeaders().getDeliveryMode());
    }


}
