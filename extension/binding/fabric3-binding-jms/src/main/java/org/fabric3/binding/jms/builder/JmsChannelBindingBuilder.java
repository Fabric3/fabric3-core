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
package org.fabric3.binding.jms.builder;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.binding.jms.spi.provision.JmsChannelBinding;
import org.fabric3.spi.container.builder.component.ChannelBindingBuilder;
import org.fabric3.spi.container.channel.Channel;

/**
 * This implementation performs a no-op as JMS infrastructure is created outside the ambit of the runtime. This can serve as an extension point for future
 * support of JMS provisioning.
 */
@Key("org.fabric3.binding.jms.spi.provision.JmsChannelBinding")
public class JmsChannelBindingBuilder implements ChannelBindingBuilder<JmsChannelBinding> {

    public void build(JmsChannelBinding binding, Channel channel)  {
        // no-op
    }

    public void dispose(JmsChannelBinding binding, Channel channel)  {
        // no-op
    }
}
