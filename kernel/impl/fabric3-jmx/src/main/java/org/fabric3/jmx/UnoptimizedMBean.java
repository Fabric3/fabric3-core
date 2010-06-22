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
package org.fabric3.jmx;

import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Rev$ $Date$
 */
public class UnoptimizedMBean extends AbstractMBean {
    private final Map<String, Interceptor> getters;
    private final Map<String, Interceptor> setters;
    private final Map<OperationKey, Interceptor> operations;

    public UnoptimizedMBean(MBeanInfo mbeanInfo,
                            Map<String, Interceptor> getters,
                            Map<String, Interceptor> setters,
                            Map<OperationKey, Interceptor> operations) {
        super(mbeanInfo);
        this.getters = getters;
        this.setters = setters;
        this.operations = operations;
    }

    public Object getAttribute(String s) throws AttributeNotFoundException, MBeanException, ReflectionException {
        Interceptor interceptor = getters.get(s);
        if (interceptor == null) {
            throw new AttributeNotFoundException(s);
        }
        return invoke(interceptor, null);
    }

    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        Interceptor interceptor = setters.get(attribute.getName());
        if (interceptor == null) {
            throw new AttributeNotFoundException(attribute.getName());
        }
        invoke(interceptor, new Object[]{attribute.getValue()});
    }

    public Object invoke(String s, Object[] objects, String[] strings) throws MBeanException, ReflectionException {
        OperationKey operation = new OperationKey(s, strings);
        Interceptor interceptor = operations.get(operation);
        if (interceptor == null) {
            throw new ReflectionException(new NoSuchMethodException(operation.toString()));
        }
        return invoke(interceptor, objects);
    }

    Object invoke(Interceptor interceptor, Object[] args) throws MBeanException, ReflectionException {
        WorkContext workContext = new WorkContext();
        workContext.addCallFrame(new CallFrame());
        Message message = new MessageImpl(args, false, workContext);
        message = interceptor.invoke(message);
        if (message.isFault()) {
            throw new MBeanException((Exception) message.getBody());
        } else {
            return message.getBody();
        }
    }
}
