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
package org.fabric3.implementation.mock.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev$ $Date$
 */
public class MockTargetInterceptor implements Interceptor {
    private Interceptor next;
    private Object mock;
    private Method method;

    public MockTargetInterceptor(Object mock, Method method) {
        this.mock = mock;
        this.method = method;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message message) {
        WorkContext old = WorkContextTunnel.getThreadWorkContext();
        try {
            Object[] args = (Object[]) message.getBody();
            WorkContextTunnel.setThreadWorkContext(message.getWorkContext());
            Object ret = method.invoke(mock, args);
            Message out = new MessageImpl();
            out.setBody(ret);

            return out;
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(old);
        }
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }


}
