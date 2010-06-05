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
package org.fabric3.implementation.pojo.component;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.component.ComponentException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.F3Conversation;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * Implementation of specification Conversation interface.
 *
 * @version $Rev$ $Date$
 */
public class ConversationImpl implements F3Conversation {
    private static final long serialVersionUID = 8249514203064252385L;
    private final Object conversationId;
    private transient ScopeContainer scopeContainer;

    /**
     * Constructor defining the conversation id.
     *
     * @param conversationID the conversation id
     * @param scopeContainer the scope container that manages instances associated with this conversation
     */
    public ConversationImpl(Object conversationID, ScopeContainer scopeContainer) {
        this.conversationId = conversationID;
        this.scopeContainer = scopeContainer;
    }

    public Object getConversationID() {
        return conversationId;
    }

    public void end() {
        if (scopeContainer == null) {
            throw new UnsupportedOperationException("Remote conversation end not supported");
        }
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        try {
            // Ensure that the conversation context is placed on the stack
            // This may not be the case if end() is called from a client component intending to end the conversation with a reference target
            CallFrame frame = new CallFrame(null, null, this, null);
            workContext.addCallFrame(frame);
            try {
                scopeContainer.stopContext(workContext);
            } catch (ComponentException e) {
                throw new ServiceRuntimeException(e);
            }
        } finally {
            workContext.popCallFrame();
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConversationImpl that = (ConversationImpl) o;
        return conversationId.equals(that.conversationId);
    }

    public int hashCode() {
        return conversationId.hashCode();
    }

    public String toString() {
        if (conversationId == null) {
            return "";
        }
        return conversationId.toString();
    }
}
