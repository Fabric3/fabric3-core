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
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.spi.domain.generator.binding;

import javax.xml.namespace.QName;

import org.fabric3.spi.model.instance.LogicalChannel;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Implementations are responsible for configuring a concrete binding for an inter-process (remote) wire or channel that uses binding.sca.
 * <p/>
 * For a given wire or chanel, a variety of transport protocols may potentially be used for the concrete binding. Which provider is selected depends on the
 * algorithm in-force in a particular domain. For example, a domain may use a weighted algorithm where a particular provider is preferred.
 */
public interface BindingProvider {

    /**
     * Returns the unique binding type.
     *
     * @return the unique binding type
     */
    QName getType();

    /**
     * Determines if this binding provider can be used as a remote transport for a wire. Implementations must take into account required intents.
     *
     * @param wire the wire
     * @return if the binding provider can wire from the source to target
     */
    BindingMatchResult canBind(LogicalWire wire);

    /**
     * Determines if this binding provider can be used as a remote transport for a service. Implementations must take into account required intents.
     *
     * @param service the service
     * @return if the binding provider can bind the service
     */
    BindingMatchResult canBind(LogicalService service);

    /**
     * Determines if this binding provider can be used as a remote transport for the channel.
     *
     * @param channel the channel
     * @return if the binding provider can be used
     */
    BindingMatchResult canBind(LogicalChannel channel);

    /**
     * Configures binding information for a wire.
     *
     * @param wire the wire
     * @throws BindingSelectionException if some error is encountered that inhibits binding configuration from being generated
     */
    void bind(LogicalWire wire) throws BindingSelectionException;

    /**
     * Configures binding information for a service.
     *
     * @param service the service
     * @throws BindingSelectionException if some error is encountered that inhibits binding configuration from being generated
     */
    void bind(LogicalService service) throws BindingSelectionException;

    /**
     * Configures binding information for a channel.
     *
     * @param channel the channel
     * @throws BindingSelectionException if some error is encountered that inhibits binding configuration from being generated
     */
    void bind(LogicalChannel channel) throws BindingSelectionException;

}
