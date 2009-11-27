/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.spi.binding.provider;

import javax.xml.namespace.QName;

import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * Implementations are responsible for configuring a binding for a reference targeted to a remote service, that is, one hosted on a different runtime.
 * Binding configuration must be performed in two situations: when the reference targets a service with an explicit binding; and when a service
 * binding is not declared.
 * <p/>
 * In the first case, the binding provider will construct a binding configuration for the reference side of the wire based on the explicitly declared
 * service binding information.
 * <p/>
 * In the second case, when no binding is specified, the reference is said to be wired to a service. In SCA, inter-VM wires use the binding.sca. This
 * binding is abstract. In other words, it represents a remote protocol the particular runtime implementation chooses to effect communication. Fabric3
 * implements binding.sca by delegating to a binding provider, which is responsible for configuring binding information for both sides (reference and
 * serivce) of a wire.
 * <p/>
 * For a given wire, a variety of transport protocols may potentially be used. Which provider is selected depends on the algorithm inforce in a
 * particular domain. For example, a domain may use a weighted algorithm where a particular provider is preferred.
 *
 * @version $Rev$ $Date$
 */
public interface BindingProvider {

    QName getType();

    /**
     * Determines if this binding provider can be used as a remote transport for the wire from the source reference to the target service.
     * Implementations must take into account required intents.
     *
     * @param source the source reference
     * @param target the target service
     * @return if the binding provider can wire from the source to target.
     */
    BindingMatchResult canBind(LogicalReference source, LogicalService target);

    /**
     * Configures binding information for the source reference and target service.
     *
     * @param source the source reference
     * @param target the target service
     * @throws BindingSelectionException if some error is encountered that inhibits binding configuration from being generated
     */
    void bind(LogicalReference source, LogicalService target) throws BindingSelectionException;

    /**
     * Configures callback binding information for the source reference and target service.
     *
     * @param source the source reference
     * @param target the target service
     * @throws BindingSelectionException if some error is encountered that inhibits callback binding configuration from being generated
     */
//    void createCallbackBinding(LogicalReference source, LogicalService target) throws BindingSelectionException;

}
