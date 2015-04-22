/*
 * Fabric3
 * Copyright (c) 2009-2015 Metaform Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 */
package org.fabric3.implementation.java.runtime;

import java.lang.reflect.Method;
import java.net.URI;

import org.fabric3.api.host.Fabric3Exception;
import org.fabric3.implementation.java.provision.JavaConnectionTarget;
import org.fabric3.implementation.pojo.component.InvokerEventStreamHandler;
import org.fabric3.implementation.pojo.spi.reflection.ConsumerInvoker;
import org.fabric3.implementation.pojo.spi.reflection.ReflectionFactory;
import org.fabric3.spi.container.builder.TargetConnectionAttacher;
import org.fabric3.spi.container.channel.ChannelConnection;
import org.fabric3.spi.container.channel.EventStream;
import org.fabric3.spi.container.channel.FilterHandler;
import org.fabric3.spi.container.component.ComponentManager;
import org.fabric3.spi.model.physical.PhysicalConnectionSource;
import org.fabric3.spi.util.UriHelper;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

/**
 * Attaches and detaches a {@link ChannelConnection} from a Java component consumer.
 */
@EagerInit
public class JavaTargetConnectionAttacher implements TargetConnectionAttacher<JavaConnectionTarget> {
    private ComponentManager manager;
    private ReflectionFactory reflectionFactory;

    public JavaTargetConnectionAttacher(@Reference ComponentManager manager, @Reference ReflectionFactory reflectionFactory) {
        this.manager = manager;
        this.reflectionFactory = reflectionFactory;
    }

    public void attach(PhysicalConnectionSource source, JavaConnectionTarget target, ChannelConnection connection) {
        URI targetUri = target.getUri();
        URI targetName = UriHelper.getDefragmentedName(targetUri);
        JavaComponent component = (JavaComponent) manager.getComponent(targetName);
        if (component == null) {
            throw new Fabric3Exception("Target component not found: " + targetName);
        }

        if (target.isDirectConnection()) {
            // A direct connection; create a setter that will inject the field, method or ctor param annotated with @Consumer
            component.setSupplier(target.getInjectable(), connection.getDirectConnection().get());
        } else {
            // Not a direct connection; a consumer method that is invoked and passed an event from the channel via an event stream
            // Note that a null supplier must be injected in this case as the @Consumer annotation creates an injector for the case where the
            // method takes a direct connection to the channel. The null supplier forces the injector not to activate since the @Consumer method is used to
            // receive events and not serve as a setter for the direct connection
            component.setSupplier(target.getInjectable(), () -> null);
            ClassLoader loader = target.getClassLoader();

            Method method = (Method) target.getConsumerObject(); // if the object is not a method, it is a programming error
            ConsumerInvoker invoker = reflectionFactory.createConsumerInvoker(method);

            EventStream stream = connection.getEventStream();

            Class<?> type = connection.getEventStream().getEventType();
            if (!Object.class.equals(type)) {
                // add a filter if the event type is not Object
                stream.addHandler(new FilterHandler(type));
            }
            InvokerEventStreamHandler handler = new InvokerEventStreamHandler(invoker, component, loader);
            stream.addHandler(handler);
        }
    }

    public void detach(PhysicalConnectionSource source, JavaConnectionTarget target) {
        // no-op
    }

}