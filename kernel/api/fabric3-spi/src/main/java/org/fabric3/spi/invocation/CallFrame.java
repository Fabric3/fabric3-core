/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.spi.invocation;

import java.io.Serializable;

/**
 * Encapsulates information for a specific invocation that is made as part of a request entering the domain. Requests may have multiple associated
 * invocations as component implementations may invoke services on other components as a request is processed.
 */
public class CallFrame implements Serializable {
    private static final long serialVersionUID = -6108279393891496098L;

    private String callbackUri;
    private Serializable correlationId;

    /**
     * Constructor. Creates a CallFrame for an invocation to a bidirectional service.
     *
     * @param callbackUri   the URI the caller of the current service can be called back on
     * @param correlationId the key used to correlate the forward invocation with the target component implementation instance. For stateless targets,
     *                      the id may be null.
     */
    public CallFrame(String callbackUri, Serializable correlationId) {
        this.callbackUri = callbackUri;
        this.correlationId = correlationId;
    }

    /**
     * Returns the URI of the callback service for the current invocation.
     *
     * @return the callback service URI or null if the invocation is to a unidirectional service.
     */
    public String getCallbackUri() {
        return callbackUri;
    }

    /**
     * Returns the key used to correlate the forward invocation with the target component implementation instance or null if the target is stateless.
     *
     * @param type the correlation id type.
     * @return the correlation id or null.
     */
    public <T extends Serializable> T getCorrelationId(Class<T> type) {
        return type.cast(correlationId);
    }

    public String toString() {
        return "CallFrame [Callback URI: " + callbackUri + " Correlation ID: " + correlationId + "]";
    }
}
