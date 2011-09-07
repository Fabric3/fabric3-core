/*
* Fabric3
* Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.implementation.drools.runtime;

import java.util.Arrays;

import org.drools.runtime.StatelessKnowledgeSession;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Handles an invocation to a Drools service using a stateless knowledge session.
 *
 * @version $Rev$ $Date$
 */
public class StatelessDroolsInterceptor implements Interceptor {
    private DroolsComponent component;
    private ClassLoader targetTCCLClassLoader;

    /**
     * Constructor.
     *
     * @param component   the target component
     * @param classloader the classloader corresponding to the rules contribution
     */
    public StatelessDroolsInterceptor(DroolsComponent component, ClassLoader classloader) {
        this.component = component;
        this.targetTCCLClassLoader = classloader;
    }

    public Message invoke(Message msg) {
        try {
            StatelessKnowledgeSession session = component.createStatelessSession();
            return executeInContext(msg, session);
        } catch (ObjectCreationException e) {
            throw new InvocationRuntimeException(e);
        }
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last one in an target interceptor chain");
    }

    public Interceptor getNext() {
        return null;
    }


    /**
     * Prepares the execution context and executes the rule set against the invocation data. If a target classloader is configured for the
     * interceptor, it will be set as the TCCL.
     *
     * @param msg     the messaging containing the invocation data
     * @param session the target knowledge session
     * @return the response message
     */
    private Message executeInContext(Message msg, StatelessKnowledgeSession session) {
        WorkContext workContext = msg.getWorkContext();
        WorkContext oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            if (targetTCCLClassLoader == null) {
                execute(msg, session);
            } else {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                try {
                   Thread.currentThread().setContextClassLoader(targetTCCLClassLoader);
                    execute(msg, session);
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldWorkContext);
        }
        return msg;
    }

    /**
     * Performs the actual rule set invocation, unwrapping the data from the message body if necessary.
     *
     * @param msg     the message containing the data
     * @param session the knowledge session
     * @return the return message
     */
    private Message execute(Message msg, StatelessKnowledgeSession session) {
        Object body = msg.getBody();
        if (body == null) {
            session.execute(body);
            msg.setBody(null);
        } else {
            if (body.getClass().isArray()) {
                // unwrap
                Object[] array = (Object[]) body;
                if (array.length == 1) {
                    Object param = array[0];
                    session.execute(param);
                    msg.setBody(param);
                } else if (array.length == 0) {
                    session.execute(array);
                    msg.setBody(array);
                } else {
                    Iterable<?> iterable = Arrays.asList(array);
                    session.execute(iterable);
                    msg.setBody(array);
                }
            } else {
                session.execute(body);
            }
        }
        return msg;
    }

}
