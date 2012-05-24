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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime.common;

import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.spi.binding.handler.BindingHandler;
import org.fabric3.spi.binding.handler.BindingHandlerRegistry;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContextTunnel;

/**
 * Utility class for managing JMS objects.
 */
public class JmsHelper {

    private JmsHelper() {
    }

    /**
     * Closes the connection quietly, ignoring exceptions.
     *
     * @param connection the connection to be closed.
     */
    public static void closeQuietly(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (JMSException ignore) {
        }
    }

    /**
     * Closes the session quietly, ignoring exceptions.
     *
     * @param session the session to be closed.
     */
    public static void closeQuietly(Session session) {
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException ignore) {
        }
    }

    /**
     * Closes the message consumer quietly, ignoring exceptions
     *
     * @param consumer the message consumer to be closed.
     */
    public static void closeQuietly(MessageConsumer consumer) {
        try {
            if (consumer != null) {
                consumer.close();
            }
        } catch (JMSException ignore) {
        }
    }
    
    /**
	 * Apply registered Binding Handlers to JMS transport message
	 * 
	 * @param context
	 * @param message
	 * @param outbound
	 * @throws JMSException 
	 */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void applyHandlers(BindingHandlerRegistry handlerRegistry ,javax.jms.Message context , Message message, String bindingName, boolean outbound) throws JMSException {
//		if (handlerRegistry == null || bindingName == null)
//			  return;
//		if (message.getWorkContext() == null){
//			message.setWorkContext(WorkContextTunnel.getThreadWorkContext());
//		}
//		List<BindingHandler<?>> handlers = handlerRegistry.loadBindingHandlers(JmsBindingDefinition.BINDING_QNAME, bindingName);
//		for (BindingHandler bh : handlers) {
//			if (outbound){
//				bh.handleOutbound(message, context);
//			}
//			else {
//				bh.handleInbound(context, message);
//			}
//		}
	}

}
