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
package org.fabric3.implementation.spring.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalConnectionTargetDefinition;
import org.fabric3.spi.model.type.java.JavaType;

/**
 * Used to attach the target side of a channel connection to a Spring bean consumer.
 */
public class SpringConnectionTargetDefinition extends PhysicalConnectionTargetDefinition {
    private static final long serialVersionUID = 967914855829805771L;
    private String beanName;
    private JavaType<?> type;
    private String methodName;

    /**
     * Constructor.
     *
     * @param beanName   the name of the bean to dispatch events to
     * @param methodName the name of the bean method to dispatch events to
     * @param type       the event type
     * @param channelUri the URI of the source channel
     */
    public SpringConnectionTargetDefinition(String beanName, String methodName, JavaType<?> type, URI channelUri) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.type = type;
        setTargetUri(channelUri);
    }

    public String getBeanName() {
        return beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public JavaType<?> getType() {
        return type;
    }

}