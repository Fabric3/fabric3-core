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
 */
package org.fabric3.runtime.weblogic.jms.runtime;

import javax.jms.ConnectionFactory;
import java.util.Map;

import org.fabric3.binding.jms.spi.runtime.manager.ConnectionFactoryManager;

/**
 * Responsible for managing WebLogic connection factories. Since WLS manages enlistment with the transaction mananger transparently, this
 * implementation does nothing.
 */
public class WebLogicConnectionFactoryManager implements ConnectionFactoryManager {

    public ConnectionFactory register(String name, ConnectionFactory factory) {
        // no-op
        return factory;
    }

    public ConnectionFactory register(String name, ConnectionFactory factory, Map<String, String> properties) {
        // no-op
        return factory;
    }

    public ConnectionFactory unregister(String name) {
        // no-op
        return null;
    }

    public ConnectionFactory get(String name) {
        return null;
    }
}
