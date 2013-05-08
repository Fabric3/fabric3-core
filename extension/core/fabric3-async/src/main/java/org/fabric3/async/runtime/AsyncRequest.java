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
package org.fabric3.async.runtime;

import java.util.List;
import java.util.Map;

import org.fabric3.api.SecuritySubject;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextCache;
import org.fabric3.spi.wire.Interceptor;

/**
 * Encapsulates an invocation to be processed asynchronously.
 */
public class AsyncRequest implements Runnable {
    private final Interceptor next;
    private final Message message;
    private SecuritySubject subject;
    private List<CallFrame> stack;
    private Map<String, Object> headers;
    private NonBlockingMonitor monitor;

    public AsyncRequest(Interceptor next,
                        Message message,
                        SecuritySubject subject,
                        List<CallFrame> stack,
                        Map<String, Object> headers,
                        NonBlockingMonitor monitor) {
        this.next = next;
        this.message = message;
        this.subject = subject;
        this.stack = stack;
        this.headers = headers;
        this.monitor = monitor;
    }

    public void run() {
        WorkContext newWorkContext = WorkContextCache.getAndResetThreadWorkContext();
        newWorkContext.addCallFrames(stack);
        newWorkContext.addHeaders(headers);
        newWorkContext.setSubject(subject);
        message.setWorkContext(newWorkContext);
        Message ret = next.invoke(message);
        if (ret.isFault()) {
            // log the exception
            monitor.onError((Throwable) ret.getBody());
        }
    }

    public Interceptor getNext() {
        return next;
    }

    public Message getMessage() {
        return message;
    }
}
