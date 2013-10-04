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
package org.fabric3.spi.generator.binding;

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
