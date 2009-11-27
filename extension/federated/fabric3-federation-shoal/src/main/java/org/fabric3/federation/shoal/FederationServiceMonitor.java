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
package org.fabric3.federation.shoal;

import org.fabric3.api.annotation.logging.Config;
import org.fabric3.api.annotation.logging.Fine;
import org.fabric3.api.annotation.logging.Finer;
import org.fabric3.api.annotation.logging.Finest;
import org.fabric3.api.annotation.logging.Info;
import org.fabric3.api.annotation.logging.Severe;
import org.fabric3.api.annotation.logging.Warning;

/**
 * Monitor for federation services.
 *
 * @version $Rev$ $Date$
 */
public interface FederationServiceMonitor {

    /**
     * Callback invoked when the runtime joins a group.
     *
     * @param groupName   the group name.
     * @param runtimeName the runtime name
     */
    @Info
    void joined(String groupName, String runtimeName);

    /**
     * Callback invoked when the runtime exits a group.
     *
     * @param groupName the domain.
     */
    @Info
    void exited(String groupName);

    /**
     * Callback invoked when the runtime joins the controller group as a zone manager.
     *
     * @param groupName   the group name.
     * @param runtimeName the runtime name
     */
    @Info
    void joinedControllerGroup(String groupName, String runtimeName);

    /**
     * Logged when an exception occurs.
     *
     * @param description the error description
     * @param domainName  the domain name
     * @param throwable   Exception that occured.
     */
    @Severe
    void onException(String description, String domainName, Throwable throwable);

    /**
     * Logged when a general exception occurs.
     *
     * @param description the error description
     * @param throwable   Exception that occured.
     */
    @Severe
    void onSignalException(String description, Throwable throwable);

    /**
     * Logged when an error occurs.
     *
     * @param description the error description
     * @param domainName  the domain name
     */
    @Severe
    void onError(String description, String domainName);

    /**
     * Callback for when a signal is received.
     *
     * @param message a message describing the signal
     */
    @Fine
    void onSignal(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Config
    void onConfig(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Info
    void onInfo(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Finer
    void onFiner(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Finest
    void onFinest(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Fine
    void onFine(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Warning
    void onWarning(String message);

    /**
     * Used for recording Shoal config-level log messages.
     *
     * @param message the message
     */
    @Severe
    void onSevere(String message);

}
