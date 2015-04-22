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
package org.fabric3.binding.zeromq.generator;

import org.fabric3.api.annotation.wire.Key;
import org.fabric3.api.binding.zeromq.model.ZeroMQBinding;
import org.fabric3.api.binding.zeromq.model.ZeroMQMetadata;
import org.fabric3.spi.domain.generator.CallbackBindingGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.oasisopen.sca.annotation.EagerInit;

/**
 *
 */
@EagerInit
@Key("org.fabric3.api.binding.zeromq.model.ZeroMQBinding")
public class ZeroMQCallbackBindingGenerator implements CallbackBindingGenerator<ZeroMQBinding> {
    public ZeroMQBinding generateServiceCallback(LogicalBinding<ZeroMQBinding> forwardBinding) {
        ZeroMQMetadata metadata = new ZeroMQMetadata();
        return new ZeroMQBinding("binding.zeromq.callback", metadata);
    }

    public ZeroMQBinding generateReferenceCallback(LogicalBinding<ZeroMQBinding> forwardBinding) {
        ZeroMQMetadata metadata = new ZeroMQMetadata();
        return new ZeroMQBinding("binding.zeromq.callback", metadata);
    }
}
